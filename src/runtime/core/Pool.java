package runtime.core;

import runtime.tni.NativeCollection;
import runtime.type.*;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;

public class Pool implements Iterable<Pool.Entry<?>> {

    private final Entry<?>[] pool;

    public Pool(int size) {
        this.pool = new Entry[size];
    }

    protected void put(int index, Entry<?> entry){
        pool[index] = entry;
    }

    public Object load(int index, TThread thread){
        return pool[index].load(thread);
    }

    @Override
    public Iterator<Entry<?>> iterator() {
        return Arrays.asList(pool).iterator();
    }

    public interface Entry<T> {
        T load(TThread thread);
    }

    public static class Int implements Entry<TInteger> {
        private final TInteger i;
        public Int(int i){
            this.i = new TInteger(i);
        }
        @Override
        public TInteger load(TThread thread) {
            return i;
        }
    }

    public static class Real implements Entry<TReal> {
        private final TReal d;
        public Real(double d){
            this.d = new TReal(d);
        }
        @Override
        public TReal load(TThread thread) {
            return d;
        }
    }

    public static class Str implements Entry<TString> {
        private final TString s;
        public Str(String s){
            this.s = new TString(s);
        }
        @Override
        public TString load(TThread thread) {
            return s;
        }
    }


    public record UTF8(String i) implements Entry<String> {
        @Override
        public String load(TThread thread) {
            return i;
        }
    }

    public static class Func implements Entry<VirtualFunction> {
        private final String name;
        private byte[][] instructions;
        private int stackSize;
        private int locals;
        private LinkedHashMap<String, Data> params;
        private Pool pool;

        public Func(String name) {
            this.name = name;
        }
        public void init(Pool pool,
                         LinkedHashMap<String, Data> params,
                         byte[][] instructions,
                         int stackSize,
                         int locals){
            this.params = params;
            this.instructions = instructions;
            this.stackSize = stackSize;
            this.locals = locals;
            this.pool = pool;
        }

        @Override
        public VirtualFunction load(TThread thread) {
            return new VirtualFunction(name, instructions, stackSize, locals, params, pool);
        }
    }

    public record Native(String name) implements Entry<Callable> {
        @Override
        public Callable load(TThread thread) {
            Callable nat = NativeCollection.load(name);
            if (nat == null)
                thread.reportRuntimeError("native function '" + name + "' does not exist");
            return nat;
        }
    }

    public static class Type implements Entry<TType> {
        private TType type;
        private int staticBlockAddress;
        @Override
        public TType load(TThread thread) {
            return type;
        }
        public void init(TType type, int staticBlockAddress){
            this.type = type;
            this.staticBlockAddress = staticBlockAddress;
        }
        public int getStaticBlockAddress() {
            return staticBlockAddress;
        }
    }



}
