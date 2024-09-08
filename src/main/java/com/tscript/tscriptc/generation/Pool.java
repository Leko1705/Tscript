package com.tscript.tscriptc.generation;

import com.tscript.tscriptc.generation.compiled.pool.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class Pool implements ConstantPool {

    private final List<PoolEntry<?>> entries = new ArrayList<>();

    @Override
    public List<PoolEntry<?>> getEntries() {
        return List.of();
    }

    public int putInt(int value) {
        return put(idx -> entries.add(new IntegerEntry(idx, value)));
    }

    public int putFloat(double value) {
        return put(idx -> entries.add(new FloatEntry(idx, value)));
    }

    public int putString(String value) {
        return put(idx -> entries.add(new StringEntry(idx, value)));
    }

    public int putBoolean(boolean value) {
        return put(idx -> entries.add(new BooleanEntry(idx, value)));
    }

    public int putNull(){
        return put(idx -> entries.add(new NullEntry(idx)));
    }

    public int putArray(int[] value) {
        List<Integer> ints = new ArrayList<>();
        for (int j : value) {
            ints.add(j);
        }
        return put(idx -> entries.add(new ArrayEntry(idx, ints)));
    }

    public int putDictionary(int[] value) {
        List<Integer> ints = new ArrayList<>();
        for (int j : value) {
            ints.add(j);
        }
        return put(idx -> entries.add(new DictionaryEntry(idx, ints)));
    }

    public int putRange(int start, int end) {
        List<Integer> ints = new ArrayList<>(List.of(start, end));
        return put(idx -> entries.add(new RangeEntry(idx, ints)));
    }

    public int putUTF8(String value) {
        return put(idx -> entries.add(new UTF8Entry(idx, value)));
    }

    private int put(Consumer<Integer> putter){
        final int next = entries.size();
        putter.accept(next);
        return next;
    }

}
