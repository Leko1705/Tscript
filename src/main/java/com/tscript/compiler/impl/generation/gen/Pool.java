package com.tscript.compiler.impl.generation.gen;

import com.tscript.compiler.impl.generation.compiled.pool.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class Pool implements ConstantPool {

    private final List<PoolEntry<?>> entries = new ArrayList<>();

    @Override
    public List<PoolEntry<?>> getEntries() {
        return entries;
    }

    public int putInt(int value) {
        return put(idx -> new IntegerEntry(idx, value));
    }

    public int putFloat(double value) {
        return put(idx -> new FloatEntry(idx, value));
    }

    public int putString(String value) {
        return put(idx -> new StringEntry(idx, value));
    }

    public int putBoolean(boolean value) {
        return put(idx -> new BooleanEntry(idx, value));
    }

    public int putNull(){
        return put(NullEntry::new);
    }

    public int putArray(int[] value) {
        List<Integer> ints = new ArrayList<>();
        for (int j : value) {
            ints.add(j);
        }
        return put(idx -> new ArrayEntry(idx, ints));
    }

    public int putDictionary(int[] value) {
        List<Integer> ints = new ArrayList<>();
        for (int j : value) {
            ints.add(j);
        }
        return put(idx -> new DictionaryEntry(idx, ints));
    }

    public int putRange(int start, int end) {
        List<Integer> ints = new ArrayList<>(List.of(start, end));
        return put(idx -> new RangeEntry(idx, ints));
    }

    public int putUTF8(String value) {
        return put(idx -> new UTF8Entry(idx, value));
    }

    private int put(Function<Integer, PoolEntry<?>> putter){
        final int next = entries.size();
        PoolEntry<?> entry = putter.apply(next);
        entries.add(entry);
        return next;
    }

}
