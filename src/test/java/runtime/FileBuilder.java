package runtime;

import com.tscript.runtime.core.Opcode;
import com.tscript.runtime.stroage.loading.LoadingConstants;
import com.tscript.runtime.typing.Member;
import com.tscript.runtime.typing.Visibility;
import com.tscript.runtime.utils.Conversion;
import com.tscript.runtime.utils.Tuple;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class FileBuilder {

    private record GlobalField(String name, boolean isMutable){}


    public static FileBuilder newBuilder(String path, String moduleName){
        return new FileBuilder(path, moduleName);
    }

    private FileBuilder(String path, String moduleName){
        this.path = path;
        this.moduleName = moduleName;
    }

    private final String path;
    private final String moduleName;
    private int entryPoint;
    private final List<GlobalField> globals = new ArrayList<>();
    private PoolBuilder pool;
    private final List<ClassBuilder> classes = new ArrayList<>();
    private final List<FunctionBuilder> functions = new ArrayList<>();


    public PoolBuilder pool() {
        return new PoolBuilder();
    }

    public ClassBuilder newClass(String className){
        return new ClassBuilder(className);
    }

    public FunctionBuilder newFunction(String functionName){
        return new FunctionBuilder(functionName);
    }

    public FileBuilder setEntryPoint(int entryPoint){
        this.entryPoint = entryPoint;
        return this;
    }

    public FileBuilder addGlobal(String fieldName, boolean isMutable){
        globals.add(new GlobalField(fieldName, isMutable));
        return this;
    }


    public File write() throws IOException {
        File file = new File(path);

        file.setWritable(true);
        file.createNewFile();

        // clear file
        PrintWriter w = new PrintWriter(file);
        w.print("");
        w.close();

        try(FileOutputStream writer = new FileOutputStream(file, true)) {
            writer.write(Conversion.to2Bytes(LoadingConstants.MAGIC_NUMBER));
            writer.write(moduleName.getBytes(StandardCharsets.UTF_8));
            writer.write('\0');
            writer.write(5);
            writer.write(5);
            writer.write(Conversion.to2Bytes(entryPoint));
            writer.write(Conversion.to2Bytes(globals.size()));
            for(GlobalField gf : globals){
                writer.write(gf.name.getBytes(StandardCharsets.UTF_8));
                writer.write('\0');
                writer.write(gf.isMutable ? 1 : 0);
            }

            if (pool == null) pool = new PoolBuilder();

            writer.write(Conversion.getBytes(pool.entries.size()));
            int i = 0;
            for (PoolBuilder.Entry entry : pool.entries){
                writer.write(Conversion.to2Bytes(i++));
                writer.write(entry.getType());
                writer.write(entry.getBytes());

            }

            writer.write(Conversion.to2Bytes(functions.size()));
            for (FunctionBuilder func : functions){
                writer.write(Conversion.to2Bytes(func.index));
                writer.write(func.name.getBytes(StandardCharsets.UTF_8));
                writer.write('\0');
                writer.write(func.parameters.size());
                for (Tuple<String, Integer> param : func.parameters){
                    writer.write(param.getFirst().getBytes(StandardCharsets.UTF_8));
                    writer.write('\0');
                    writer.write(Conversion.to2Bytes(param.getSecond()));
                }
                writer.write(Conversion.to2Bytes(func.stackSize));
                writer.write(Conversion.to2Bytes(func.locals));
                writer.write(Conversion.getBytes(func.instructions.size()));
                for (FunctionBuilder.Instruction ins : func.instructions){
                    writer.write(ins.opcode.ordinal());
                    for (int i1 : ins.args){
                        writer.write(i1);
                    }
                }
            }
            writer.write(Conversion.to2Bytes(classes.size()));
            for (ClassBuilder cb : classes){
                writer.write(Conversion.to2Bytes(cb.index));
                writer.write(cb.name.getBytes(StandardCharsets.UTF_8));
                writer.write('\0');
                writer.write(Conversion.to2Bytes(cb.superTypeRef));
                writer.write(cb.isAbstract ? 1 : 0);
                writer.write(Conversion.to2Bytes(cb.constructorIndex));
                writer.write(Conversion.to2Bytes(cb.staticBlockInitializer));
                writer.write(Conversion.to2Bytes(cb.staticMembers.size()));
                for (Member mem : cb.staticMembers){
                    writer.write(mem.getName().getBytes(StandardCharsets.UTF_8));
                    writer.write('\0');
                    int spec = 0;
                    if (mem.getVisibility() == Visibility.PUBLIC) spec = 1;
                    else if (mem.getVisibility() == Visibility.PROTECTED) spec = 2;
                    else if (mem.getVisibility() == Visibility.PRIVATE) spec = 4;
                    if (!mem.isMutable()) spec |= 8;
                    writer.write(spec);
                }
                writer.write(Conversion.to2Bytes(cb.instanceMembers.size()));
                for (Member mem : cb.instanceMembers){
                    writer.write(mem.getName().getBytes(StandardCharsets.UTF_8));
                    writer.write('\0');
                    int spec = 0;
                    if (mem.getVisibility() == Visibility.PUBLIC) spec = 1;
                    else if (mem.getVisibility() == Visibility.PROTECTED) spec = 2;
                    else if (mem.getVisibility() == Visibility.PRIVATE) spec = 4;
                    if (!mem.isMutable()) spec |= 8;
                    writer.write(spec);
                }
            }
        }
        return file;
    }





    public class PoolBuilder {

        private List<Entry> entries = new ArrayList<>();

        private interface Entry {
            int getType();
            byte[] getBytes();
        }

        public PoolBuilder addInteger(int value){
            entries.add(new Entry() {
                @Override
                public int getType() {
                    return LoadingConstants.POOL_TYPE_INT;
                }

                @Override
                public byte[] getBytes() {
                    return Conversion.getBytes(value);
                }
            });
            return this;
        }

        public PoolBuilder addReal(double value){
            entries.add(new Entry() {
                @Override
                public int getType() {
                    return LoadingConstants.POOL_TYPE_REAL;
                }

                @Override
                public byte[] getBytes() {
                    return Conversion.getBytes(value);
                }
            });
            return this;
        }

        public PoolBuilder addString(String value){
            entries.add(new Entry() {
                @Override
                public int getType() {
                    return LoadingConstants.POOL_TYPE_STRING;
                }

                @Override
                public byte[] getBytes() {
                    byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
                    byte[] longer = new byte[bytes.length + 1];
                    System.arraycopy(bytes, 0, longer, 0, bytes.length);
                    longer[bytes.length] = '\0';
                    return longer;
                }
            });
            return this;
        }

        public PoolBuilder addBoolean(boolean value){
            entries.add(new Entry() {
                @Override
                public int getType() {
                    return LoadingConstants.POOL_TYPE_BOOL;
                }

                @Override
                public byte[] getBytes() {
                    return value ? Conversion.getBytes(1) : Conversion.getBytes(0);
                }
            });
            return this;
        }

        public PoolBuilder addNull(){
            entries.add(new Entry() {
                @Override
                public int getType() {
                    return LoadingConstants.POOL_TYPE_NULL;
                }

                @Override
                public byte[] getBytes() {
                    return new byte[0];
                }
            });
            return this;
        }

        public PoolBuilder addRange(int start, int end){
            entries.add(new Entry() {
                @Override
                public int getType() {
                    return LoadingConstants.POOL_TYPE_RANGE;
                }

                @Override
                public byte[] getBytes() {
                    return new byte[]{Conversion.to2Bytes(start)[0], Conversion.to2Bytes(start)[1],
                            Conversion.to2Bytes(end)[0], Conversion.to2Bytes(end)[1]};
                }
            });
            return this;
        }

        public PoolBuilder addArray(int... content){
            entries.add(new Entry() {
                @Override
                public int getType() {
                    return LoadingConstants.POOL_TYPE_ARRAY;
                }

                @Override
                public byte[] getBytes() {
                    byte[] bytes = new byte[content.length * 2 + 1];
                    bytes[0] = (byte) content.length;
                    for (int i = 1; i < content.length; i += 2){
                        byte[] asBytes = Conversion.to2Bytes(content[i]);
                        bytes[i] = asBytes[0];
                        bytes[i+1] = asBytes[0];
                    }
                    return bytes;
                }
            });
            return this;
        }

        public PoolBuilder addUTF8(String value){
            entries.add(new Entry() {
                @Override
                public int getType() {
                    return LoadingConstants.POOL_TYPE_UTF8;
                }

                @Override
                public byte[] getBytes() {
                    byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
                    byte[] longer = new byte[bytes.length + 1];
                    System.arraycopy(bytes, 0, longer, 0, bytes.length);
                    longer[bytes.length] = '\0';
                    return longer;
                }
            });
            return this;
        }

        public FileBuilder complete(){
            pool = this;
            return FileBuilder.this;
        }

    }


    public class ClassBuilder {
        private final String name;
        private final int index;

        private final List<Member> staticMembers = new ArrayList<>();
        private final List<Member> instanceMembers = new ArrayList<>();

        private int superTypeRef = -1;
        private boolean isAbstract = false;
        private int constructorIndex = -1;
        private int staticBlockInitializer = -1;

        public ClassBuilder(String name) {
            this.name = name;
            this.index = classes.size();
        }

        public ClassBuilder withSuperType(int superTypeRef){
            this.superTypeRef = superTypeRef;
            return this;
        }

        public ClassBuilder setAbstract(boolean isAbstract){
            this.isAbstract = isAbstract;
            return this;
        }

        public ClassBuilder withConstructor(int index){
            constructorIndex = index;
            return this;
        }

        public ClassBuilder withStaticBlockInitializer(int index){
            this.staticBlockInitializer = index;
            return this;
        }

        public ClassBuilder withStaticMember(String name, Visibility visibility, boolean isMutable){
            staticMembers.add(Member.of(visibility, isMutable, name, null));
            return this;
        }

        public ClassBuilder withStaticInstance(String name, Visibility visibility, boolean isMutable){
            instanceMembers.add(Member.of(visibility, isMutable, name, null));
            return this;
        }

        public FileBuilder register(){
            classes.add(this);
            return FileBuilder.this;
        }

    }

    public class FunctionBuilder {

        record Instruction(Opcode opcode, int... args){}

        private final String name;
        private final int index;
        private int locals = 0;
        private int stackSize = 0;

        private final List<Tuple<String, Integer>> parameters = new ArrayList<>();
        private final List<Instruction> instructions = new ArrayList<>();

        public FunctionBuilder(String name) {
            this.name = name;
            this.index = functions.size();
        }

        public FunctionBuilder withParameter(String name, Integer poolAddr){
            if (poolAddr == null) poolAddr = -1;
            parameters.add(new Tuple<>(name, poolAddr));
            return this;
        }

        public FunctionBuilder appendInstruction(Opcode opcode, int... args){
            instructions.add(new Instruction(opcode, args));
            return this;
        }

        public FunctionBuilder withLocals(int locals){
            this.locals = locals;
            return this;
        }

        public FunctionBuilder withStackSize(int stackSize){
            this.stackSize = stackSize;
            return this;
        }

        public FileBuilder register(){
            functions.add(this);
            return FileBuilder.this;
        }
    }

}
