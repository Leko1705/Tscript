package runtime.core;

import runtime.type.*;
import tscriptc.generation.Opcode;
import tscriptc.generation.Type;
import tscriptc.tree.Modifier;
import tscriptc.util.Conversion;

import java.io.File;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.util.*;

public class FileLoader {
    private static final int magicNumber = 0xDEAD;


    private final byte[] content;
    private int index = 0;

    private int minor;
    private int major;
    private int entryPoint;
    private Pool pool;

    private int globals;

    private Map<String, Pool.Func> virtualFunctions;
    private Map<String, Pool.Type> types;

    private final TscriptVM vm;
    private TThread thread;

    public FileLoader(File file, TscriptVM vm, TThread thread) {
        this.vm = vm;
        this.thread = thread;
        try (FileInputStream in = new FileInputStream(file)){
            this.content = in.readAllBytes();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void load(){

        if (loadInt() != magicNumber)
            throw new IllegalStateException("invalid magic number");

        minor = loadInt();
        major = loadInt();
        entryPoint = loadInt();
        globals = loadInt();

        loadPool();
        loadFunctions();
        loadTypes();
    }

    private void loadPool(){
        int poolSize = loadInt();
        pool = new Pool(poolSize);

        virtualFunctions = new HashMap<>();
        types = new HashMap<>();

        for (int i = 0; i < poolSize; i++){
            byte kind = consume();
            switch (kind){
                case 0 -> pool.put(i, new Pool.Int(loadInt()));
                case 1 -> pool.put(i, new Pool.Real(loadReal()));
                case 2 -> pool.put(i, new Pool.Str(loadString()));
                case 3 -> pool.put(i, new Pool.UTF8(loadString()));
                case 4 -> {
                    String name = loadString();
                    Pool.Func func = new Pool.Func(name);
                    pool.put(i, func);
                    virtualFunctions.put(name, func);
                }
                case 5 -> pool.put(i, new Pool.Native(loadString()));
                case 6 -> {
                    String name = loadString();
                    Pool.Type type = new Pool.Type();
                    pool.put(i, type);
                    types.put(name, type);
                }
                case 7 -> pool.put(i, new Pool.Bool(consume() == 1 ? TBoolean.TRUE : TBoolean.FALSE));
                case 8 -> pool.put(i, new Pool.Null());
                case 9 -> {
                    int length = consume();
                    int[] references = new int[length];
                    for (int j = 0; j < length; j++)
                        references[j] = consume();
                    pool.put(i, new Pool.Array(references));
                }
                case 10 -> {
                    int length = consume();
                    int[] keyRefs = new int[length];
                    int[] valRefs = new int[length];
                    for (int j = 0; j < length; j++) {
                        keyRefs[j] = consume();
                        valRefs[j] = consume();
                    }
                    pool.put(i, new Pool.Dict(keyRefs, valRefs));
                }
                case 11 -> pool.put(i, new Pool.Range(consume(), consume()));
                case 12 -> pool.put(i, new Pool.Import(loadString(), vm));
            }
        }

    }

    private void loadFunctions(){
        int amount = loadInt();
        for (int i = 0; i < amount; i++){
            String name = loadString();
            int paramc = loadInt();
            LinkedHashMap<String, Data> params = new LinkedHashMap<>();
            for (int k = 0; k < paramc; k++){
                String paramName = loadString();
                int poolAddress = consume();
                Data defaultValue = null;
                if (poolAddress >= 0)
                    defaultValue = (Data) pool.load(poolAddress, null);
                params.put(paramName, defaultValue);
            }
            int stackSize = loadInt();
            int locals = loadInt();
            byte[][] instructions = loadInstructions();

            Pool.Func func = virtualFunctions.get(name);
            func.init(params, instructions, stackSize, locals);
        }
    }

    private byte[][] loadInstructions(){
        int amount = loadInt();
        byte[][] instructions = new byte[amount][];

        for (int i = 0; i < amount; i++) {
            Opcode opc = Opcode.of(consume());
            byte[] args = new byte[1 + opc.argc];

            args[0] = opc.b;
            for (int k = 0; k < opc.argc; k++)
                args[k+1] = consume();

            instructions[i] = args;
        }

        return instructions;
    }

    private void loadTypes(){
        int amount = loadInt();
        Map<String, String> inheritances = new HashMap<>();

        for (int i = 0; i < amount; i++){
            String name = loadString();

            String superName = null;
            boolean hasSuperType = consume() == 1;
            if (hasSuperType) superName = loadString();
            inheritances.put(name, superName);

            boolean isAbstract = consume() == 1;
            int constructorAddress = loadInt();
            int staticBlockAddress = loadInt();
            int memberAmount = loadInt();

            List<Member> statics = new ArrayList<>();

            List<Member> members = new ArrayList<>();

            for (int j = 0; j < memberAmount; j++) {
                String memberName = loadString();
                int specs = loadInt();
                Visibility v = getVisibility(specs);

                List<Member> list = isStatic(specs) ? statics : members;
                Member m = new Member(memberName, v, (specs >> 5) == 1 ? Member.Kind.IMMUTABLE : Member.Kind.MUTABLE, null);
                list.add(m);
            }
            TType type = new TType(name, null, isAbstract, statics, members);

            if (constructorAddress >= 0) {
                Callable constructor = (Callable) pool.load(constructorAddress, null);
                type.setConstructor(constructor);
            }
            Pool.Type constant = types.get(name);
            constant.init(type, staticBlockAddress);
        }

        // apply inheritances
        for (String type : inheritances.keySet()){
            String superType = inheritances.get(type);
            if (superType == null) continue;
            TType t = types.get(type).load();
            t.setSuperType(types.get(superType).load());
        }

        // invoke all statics
        for (Pool.Type t : types.values()){
            TType type = t.load();
            Callable staticBlock = (Callable) pool.load(t.getStaticBlockAddress(), null);
            staticBlock.setOwner(type);
            if (thread == null) {
                thread = new TThread(vm, staticBlock, 0);
                thread.begin();
            }
            else {
                thread.call(staticBlock, List.of());
            }
        }
    }


    private Visibility getVisibility(int specs){
        if (specs % 2 == 1) return Visibility.PUBLIC;
        else if (((specs >> 1) & 1) == 1) return Visibility.PROTECTED;
        else if (((specs >> 2) & 1) == 1) return Visibility.PRIVATE;
        throw new IllegalStateException("no visibility given");
    }

    private boolean isStatic(int specs){
        return (specs >> 3) == 1;
    }

    public int getMinor() {
        return minor;
    }

    public int getMajor() {
        return major;
    }

    public int getEntryPoint() {
        return entryPoint;
    }

    public int getGlobals() {
        return globals;
    }

    public Pool getPool() {
        return pool;
    }

    private byte consume(){
        if (index >= content.length)
            throw new IllegalStateException("invalid bytecode");
        return content[index++];
    }

    private int loadInt() {
        return ((consume() & 0xFF) << 24) |
                ((consume() & 0xFF) << 16) |
                ((consume() & 0xFF) << 8 ) |
                ((consume() & 0xFF));
    }

    private double loadReal(){
        byte[] bytes = {
                consume(), consume(), consume(), consume(),
                consume(), consume(), consume(), consume()};
        return ByteBuffer.wrap(bytes).getDouble();
    }

    private String loadString(){
        StringBuilder sb = new StringBuilder();
        byte b;
        while ((b = consume()) != '\0')
            sb.append((char) b);
        return sb.toString();
    }
}
