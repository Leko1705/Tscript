package runtime.core;

import runtime.tni.NativeCollection;
import runtime.tni.NativeFunction;
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
        return pool[index].load(thread, this);
    }

    @Override
    public Iterator<Entry<?>> iterator() {
        return Arrays.asList(pool).iterator();
    }

    public interface Entry<T> {
        T load(TThread thread, Pool pool);
    }

    public static class Int implements Entry<TInteger> {
        private final TInteger i;
        public Int(int i){
            this.i = new TInteger(i);
        }
        @Override
        public TInteger load(TThread thread, Pool pool) {
            return i;
        }
    }

    public static class Real implements Entry<TReal> {
        private final TReal d;
        public Real(double d){
            this.d = new TReal(d);
        }
        @Override
        public TReal load(TThread thread, Pool pool) {
            return d;
        }
    }

    public static class Str implements Entry<TString> {
        private final TString s;
        public Str(String s){
            this.s = new TString(s);
        }
        @Override
        public TString load(TThread thread, Pool pool) {
            return s;
        }
    }


    public record UTF8(String i) implements Entry<String> {
        @Override
        public String load(TThread thread, Pool pool) {
            return i;
        }
    }

    public static class Func implements Entry<VirtualFunction> {
        private final String name;
        private byte[][] instructions;
        private int stackSize;
        private int locals;
        private LinkedHashMap<String, Data> params;

        public Func(String name) {
            this.name = name;
        }
        public void init(LinkedHashMap<String, Data> params,
                         byte[][] instructions,
                         int stackSize,
                         int locals){
            this.params = params;
            this.instructions = instructions;
            this.stackSize = stackSize;
            this.locals = locals;
        }

        @Override
        public VirtualFunction load(TThread thread, Pool pool) {
            return new VirtualFunction(name, instructions, stackSize, locals, params, pool);
        }
    }

    public record Native(String name) implements Entry<NativeFunction> {
        @Override
        public NativeFunction load(TThread thread, Pool pool) {
            NativeFunction nat = NativeCollection.load(name);
            if (nat == null && thread != null)
                thread.reportRuntimeError("native function '" + name + "' does not exist");
            return nat;
        }
    }

    public static class Type implements Entry<TType> {
        private TType type;
        private int staticBlockAddress;
        public TType load(){
            return type;
        }
        @Override
        public TType load(TThread thread, Pool pool) {
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


    public record Bool(TBoolean bool) implements Entry<TBoolean> {
        @Override
        public TBoolean load(TThread thread, Pool pool) {
            return bool;
        }
    }

    public static class Null implements Entry<TNull> {
        @Override
        public TNull load(TThread thread, Pool pool) {
            return TNull.NULL;
        }
    }

    public static class Array implements Entry<TArray> {
        private final int[] references;

        public Array(int[] references) {
            this.references = references;
        }

        @Override
        public TArray load(TThread thread, Pool pool) {
            TArray array = new TArray();
            for (int ref : references)
                array.get().add((Data) pool.load(ref, thread));
            return array;
        }
    }

    public static class Dict implements Entry<TDictionary> {
        private final int[] keyRefs, valueRefs;

        public Dict(int[] keyRefs, int[] valueRefs) {
            this.keyRefs = keyRefs;
            this.valueRefs = valueRefs;
        }

        @Override
        public TDictionary load(TThread thread, Pool pool) {
            TDictionary dict = new TDictionary();
            for (int i = 0; i < keyRefs.length; i++){
                Data key = (Data) pool.load(keyRefs[i], thread);
                Data value = (Data) pool.load(valueRefs[i], thread);
                dict.get().put(key, value);
            }
            return dict;
        }
    }

    public record Range(int fromAddress, int toAddress) implements Entry<TRange> {

        @Override
        public TRange load(TThread thread, Pool pool) {

            return new TRange((TInteger) pool.load(fromAddress, thread), (TInteger) pool.load(toAddress, thread));
        }
    }
}
