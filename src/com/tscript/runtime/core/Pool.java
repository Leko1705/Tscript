package com.tscript.runtime.core;

import com.tscript.runtime.tni.NativeCollection;
import com.tscript.runtime.tni.NativeFunction;
import com.tscript.runtime.type.*;

import java.io.File;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;

public class Pool implements Iterable<Pool.Entry<?>> {

    private final Entry<?>[] pool;
    private Reference moduleRef;


    public Pool(int size) {
        this.pool = new Entry[size];
    }

    protected void put(int index, Entry<?> entry){
        pool[index] = entry;
    }

    protected void setModuleReference(Reference moduleRef) {
        if (this.moduleRef != null)
            throw new AssertionError("can not set owner-module twice");
        this.moduleRef = moduleRef;
    }

    public Object loadUnsafe(int index) throws PoolLoadingException {
        return pool[index].load(this);
    }

    public Data loadData(int index){
        return (Data) pool[index].load(this);
    }

    public String loadString(int index){
        return (String) pool[index].load(this);
    }

    @Override
    public Iterator<Entry<?>> iterator() {
        return Arrays.asList(pool).iterator();
    }

    public interface Entry<T> {
        T load(Pool pool);
        String toString();
    }

    public static class Int implements Entry<TInteger> {
        private final TInteger i;
        public Int(int i){
            this.i = new TInteger(i);
        }
        @Override
        public TInteger load(Pool pool) {
            return i;
        }

        @Override
        public String toString() {
            return Integer.toString(i.get());
        }
    }

    public static class Real implements Entry<TReal> {
        private final TReal d;
        public Real(double d){
            this.d = new TReal(d);
        }
        @Override
        public TReal load(Pool pool) {
            return d;
        }

        @Override
        public String toString() {
            return Double.toString(d.get());
        }
    }

    public static class Str implements Entry<TString> {
        private final TString s;
        public Str(String s){
            this.s = new TString(s);
        }
        @Override
        public TString load(Pool pool) {
            return s;
        }

        @Override
        public String toString() {
            return s.get();
        }
    }


    public record UTF8(String i) implements Entry<String> {
        @Override
        public String load(Pool pool) {
            return i;
        }

        @Override
        public String toString() {
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
        public VirtualFunction load(Pool pool) {
            return new VirtualFunction(name, instructions, stackSize, locals, params, pool, pool.moduleRef);
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public record Native(String name) implements Entry<NativeFunction> {
        @Override
        public NativeFunction load(Pool pool) {
            NativeFunction nat = NativeCollection.load(name);
            if (nat == null)
                throw new PoolLoadingException("native function '" + name + "' does not exist");
            return nat;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public static class Type implements Entry<TType> {
        private TType type;
        private int staticBlockAddress;

        public TType load(){
            return type;
        }
        @Override
        public TType load(Pool pool) {
            return type;
        }
        public void init(TType type, int staticBlockAddress){
            this.type = type;
            this.staticBlockAddress = staticBlockAddress;
        }
        public int getStaticBlockAddress() {
            return staticBlockAddress;
        }

        @Override
        public String toString() {
            return type.getName();
        }
    }


    public record Bool(TBoolean bool) implements Entry<TBoolean> {
        @Override
        public TBoolean load(Pool pool) {
            return bool;
        }

        @Override
        public String toString() {
            return Boolean.toString(bool.get());
        }
    }

    public static class Null implements Entry<TNull> {
        @Override
        public TNull load(Pool pool) {
            return TNull.NULL;
        }
        @Override
        public String toString() {
            return "null";
        }
    }

    public static class Array implements Entry<TArray> {
        private final int[] references;

        public Array(int[] references) {
            this.references = references;
        }

        @Override
        public TArray load(Pool pool) {
            TArray array = new TArray();
            for (int ref : references)
                array.get().add((Data) pool.loadData(ref));
            return array;
        }
        @Override
        public String toString() {
            return Arrays.toString(references);
        }
    }

    public static class Dict implements Entry<TDictionary> {
        private final int[] keyRefs, valueRefs;

        public Dict(int[] keyRefs, int[] valueRefs) {
            this.keyRefs = keyRefs;
            this.valueRefs = valueRefs;
        }

        @Override
        public TDictionary load(Pool pool) {
            TDictionary dict = new TDictionary();
            for (int i = 0; i < keyRefs.length; i++){
                Data key = pool.loadData(keyRefs[i]);
                Data value = pool.loadData(valueRefs[i]);
                dict.get().put(key, value);
            }
            return dict;
        }

        @Override
        public String toString() {
            return Arrays.toString(keyRefs) + Arrays.toString(valueRefs);
        }
    }

    public record Range(int fromAddress, int toAddress) implements Entry<TRange> {

        @Override
        public TRange load(Pool pool) {
            return new TRange((TInteger) pool.loadData(fromAddress), (TInteger) pool.loadData(toAddress));
        }

        @Override
        public String toString() {
            return fromAddress + ":" + toAddress;
        }
    }

    public static class Import implements Entry<Object> {
        private int address = -1;
        private final String path;
        private final String toImport;

        public Import(String s) {
            String[] full = s.split("[.]");
            toImport = full[full.length-1];
            StringBuilder path = new StringBuilder(full[0]);
            for (int i = 1; i < full.length-1; i++)
                path.append(File.separator).append(full[i]);
            this.path = path.append(".com.tscript.tscriptc").toString();
        }

        @Override
        public Object load(Pool pool) {
            return null;
        }

        @Override
        public String toString() {
            return path + File.separator + toImport;
        }
    }

}
