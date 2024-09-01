package com.tscript.runtime.stroage.loading;

import com.tscript.runtime.core.*;
import com.tscript.runtime.stroage.Module;
import com.tscript.runtime.stroage.Pool;
import com.tscript.runtime.stroage.TypeArea;
import com.tscript.runtime.typing.*;
import com.tscript.runtime.utils.Conversion;
import com.tscript.runtime.utils.Tuple;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public class ModuleLoaderImpl implements ModuleLoader, LoadingConstants {

    private final FilePathResolver resolver;
    private final Map<String, Module> cache;

    public ModuleLoaderImpl(FilePathResolver resolver) {
        this.resolver = resolver;
        this.cache = new HashMap<>();
    }

    @Override
    public synchronized Module loadModule(File[] rootPaths, String[] moduleName) throws ModuleLoadingException {
        String canonicalPath = makeCanonicalFilePath(moduleName);
        Module module = cache.get(canonicalPath);
        if (module != null) return module;
        File file = resolver.resolve(rootPaths, moduleName);
        if (file == null) throw new ModuleLoadingException(InternalRuntimeErrorMessages.canNotFindModule(canonicalPath));
        module = loadModule(file);
        cache.put(canonicalPath, module);
        return module;
    }

    private static String makeCanonicalFilePath(String[] path){
        StringBuilder sb = new StringBuilder(path[0]);
        for (int i = 1; i < path.length; i++){
            sb.append(".").append(path[i]);
        }
        return sb.toString();
    }

    public Module loadModule(File file) throws ModuleLoadingException {
        LazyReader reader = new LazyReader(file);

        // skip the magic number.
        // Was checked earlier in filePathResolving.
        reader.read();
        reader.read();

        String canonicalPath = reader.readString();

        int minor = reader.read();
        int major = reader.read();

        int entryPoint = Conversion.from2Bytes(reader.read(), reader.read());

        Member[] members = loadGlobalNamedRegisters(reader);

        Pool pool = loadPool(reader);
        LoadedFunctionArea functionArea = loadFunctions(reader);
        TypeArea typeArea = loadTypes(reader, pool);

        return new LoadedModule(
                file.getPath(),
                canonicalPath,
                major,
                minor,
                pool,
                members,
                entryPoint,
                functionArea,
                typeArea);
    }

    private Member[] loadGlobalNamedRegisters(LazyReader reader) throws ModuleLoadingException {
        int globalAmount = Conversion.from2Bytes(reader.read(), reader.read());
        Member[] members = new Member[globalAmount];
        for (int i = 0; i < globalAmount; i++) {
            String name = reader.readString();
            byte mutableByte = reader.read();
            members[i] = new Member(Visibility.PUBLIC, mutableByte == 1, name, Null.INSTANCE);
        }
        return members;
    }

    private Pool loadPool(LazyReader reader) throws ModuleLoadingException {
        int size = reader.readInt();
        PoolBuilder builder = new PoolBuilder(size);

        for (int i = 0; i < size; i++) {
            int id = Conversion.from2Bytes(reader.read(), reader.read());
            byte type = reader.read();
            switch (type) {
                case POOL_TYPE_INT -> builder.put(id, new PoolBuilder.IntEntry(reader.readInt()));
                case POOL_TYPE_REAL -> builder.put(id, new PoolBuilder.RealEntry(reader.readDouble()));
                case POOL_TYPE_STRING -> builder.put(id, new PoolBuilder.StringEntry(reader.readString()));
                case POOL_TYPE_BOOL -> builder.put(id, new PoolBuilder.BooleanEntry(TBoolean.of(reader.read() == 1)));
                case POOL_TYPE_NULL -> builder.put(id, new PoolBuilder.NullEntry());
                case POOL_TYPE_RANGE -> builder.put(id, new PoolBuilder.RangeEntry(reader.read(), reader.read(), reader.read(), reader.read()));
                case POOL_TYPE_ARRAY -> {
                    int length = reader.read();
                    byte[] references = new byte[length*2];
                    for (int j = 0; j < length; j += 2) {
                        references[j] = reader.read();
                        references[j + 1] = reader.read();
                    }
                    builder.put(id, new PoolBuilder.ArrayEntry(references));
                }
                case POOL_TYPE_DICTIONARY -> {
                    int length = reader.read();
                    byte[] references = new byte[length*4];
                    for (int j = 0; j < length; j += 4) {
                        references[j] = reader.read();
                        references[j + 1] = reader.read();
                        references[j + 2] = reader.read();
                        references[j + 3] = reader.read();
                    }
                    builder.put(id, new PoolBuilder.DictionaryEntry(references));
                }
                case POOL_TYPE_UTF8 -> builder.put(id, new PoolBuilder.UTF8Entry(reader.readString()));
            }
        }

        return builder;
    }

    private LoadedFunctionArea loadFunctions(LazyReader reader) throws ModuleLoadingException {
        int functionAmount = Conversion.from2Bytes(reader.read(), reader.read());
        LoadedFunctionArea area = new LoadedFunctionArea(functionAmount);

        for (int i = 0; i < functionAmount; i++) {
            int id = Conversion.from2Bytes(reader.read(), reader.read());
            String name = reader.readString();
            Tuple<String, byte[]>[] params = loadParams(reader);
            int stackSize = Conversion.from2Bytes(reader.read(), reader.read());
            int locals = Conversion.from2Bytes(reader.read(), reader.read());
            byte[][] instructions = loadInstructions(reader);
            area.data[id] = new LoadedFunctionArea.VirtualFunctionMetaData(name, params, stackSize, locals, instructions);
        }

        return area;
    }

    @SuppressWarnings("unchecked")
    private Tuple<String, byte[]>[] loadParams(LazyReader reader) throws ModuleLoadingException {
        byte paramCount = reader.read();
        Tuple<String, byte[]>[] params = new Tuple[paramCount];

        for (int j = 0; j < paramCount; j++) {
            String paramName = reader.readString();
            byte p1 = reader.read();
            byte p2 = reader.read();
            params[j] = new Tuple<>(paramName, new byte[] {p1, p2});
        }

        return params;
    }

    private byte[][] loadInstructions(LazyReader reader) throws ModuleLoadingException {
        int amount = reader.readInt();
        byte[][] instructions = new byte[amount][];

        for (int i = 0; i < amount; i++) {
            Opcode opc = Opcode.of(reader.read());
            byte[] args = new byte[1 + opc.argc];

            args[0] = opc.b;
            for (int k = 0; k < opc.argc; k++)
                args[k+1] = reader.read();

            instructions[i] = args;
        }

        return instructions;
    }

    private TypeArea loadTypes(LazyReader reader, Pool pool) throws ModuleLoadingException {
        int typeAmount = Conversion.from2Bytes(reader.read(), reader.read());
        LoadedTypeArea area = new LoadedTypeArea(typeAmount);

        int[] inheritors = new int[typeAmount];

        for (int i = 0; i < typeAmount; i++) {
            int id = Conversion.from2Bytes(reader.read(), reader.read());

            String name = reader.readString();

            inheritors[id] = Conversion.from2Bytes(reader.read(), reader.read());

            boolean isAbstract = reader.read() == 1;

            Function constructor = loadSpecialTypeMethod(reader, pool);
            Function staticBlock = loadSpecialTypeMethod(reader, pool);

            int staticMemberAmount = Conversion.from2Bytes(reader.read(), reader.read());
            Member[] staticMembers = readMembers(reader, staticMemberAmount);

            int instanceMemberAmount = Conversion.from2Bytes(reader.read(), reader.read());
            Member[] instanceMembers = readMembers(reader, instanceMemberAmount);

            area.virtualTypes[id] = new VirtualType(name, isAbstract, constructor, staticMembers, instanceMembers);
            area.unloadedStaticBlocks[id] = staticBlock;
        }

        for (int id = 0; id < typeAmount; id++) {
            int ref = inheritors[id];
            if (ref == -1) continue;
            VirtualType type = area.virtualTypes[id];
            VirtualType superType = area.virtualTypes[ref];
            type.setSuperType(superType);
            type.notifySuperTypeInitialized();
        }

        return area;
    }

    private Function loadSpecialTypeMethod(LazyReader reader, Pool pool) throws ModuleLoadingException {
        byte b1 = reader.read();
        byte b2 = reader.read();
        if (Conversion.from2Bytes(b1, b2) == -1) return null;
        return (Function) pool.loadConstant(b1, b2);
    }

    private Member[] readMembers(LazyReader reader, int amount) throws ModuleLoadingException {
        Member[] members = new Member[amount];
        for (int j = 0; j < amount; j++) {
            String memberName = reader.readString();
            byte specs = reader.read();

            Visibility visibility = getVisibility(specs);
            boolean isMutable = isMutable(specs);

            members[j] = new Member(visibility, isMutable, memberName, Null.INSTANCE);
        }
        return members;
    }

    private Visibility getVisibility(byte specs){
        if (specs % 2 == 1) return Visibility.PUBLIC;
        else if (((specs >> 1) & 1) == 1) return Visibility.PROTECTED;
        else if (((specs >> 2) & 1) == 1) return Visibility.PRIVATE;
        return Visibility.PUBLIC;
    }

    private boolean isMutable(byte specs){
        return (specs >> 4) != 1;
    }

    private static class LazyReader {
        private final FileReader reader;

        private LazyReader(File file) {
            try{
                reader = new FileReader(file);
            }
            catch (IOException e) {
                throw new AssertionError();
            }
        }

        public byte read() throws ModuleLoadingException {
            try {
                int c = reader.read();
                if (c == -1) throw new ModuleLoadingException("unexpected end of file");
                return (byte) c;
            }
            catch (IOException e){
                throw new ModuleLoadingException(e.getMessage());
            }
        }

        private int readInt() throws ModuleLoadingException {
            return Conversion.fromBytes(read(), read(), read(), read());
        }

        private double readDouble() throws ModuleLoadingException {
            byte[] bytes = {
                    read(), read(), read(), read(),
                    read(), read(), read(), read()};
            return ByteBuffer.wrap(bytes).getDouble();
        }

        private String readString() throws ModuleLoadingException {
            StringBuilder sb = new StringBuilder();
            byte b;
            while ((b = read()) != '\0')
                sb.append((char) b);
            return sb.toString();
        }

    }

}
