package com.tscript.tscriptc.generation.compiled.pool;

import com.tscript.tscriptc.generation.writers.PoolEntryWriter;

import java.util.List;

public class ArrayEntry extends BasePoolEntry<List<Integer>> {

    protected ArrayEntry(int index, List<Integer> value) {
        super(index, value);
    }

    @Override
    public PoolTag getTag() {
        return PoolTag.ARRAY;
    }

    @Override
    public void write(PoolEntryWriter writer) {
        writer.writeArray(this);
    }

}
