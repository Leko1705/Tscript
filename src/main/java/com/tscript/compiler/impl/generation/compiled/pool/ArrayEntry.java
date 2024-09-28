package com.tscript.compiler.impl.generation.compiled.pool;

import com.tscript.compiler.impl.generation.writers.PoolEntryWriter;

import java.util.List;

public class ArrayEntry extends BasePoolEntry<List<Integer>> {

    public ArrayEntry(int index, List<Integer> value) {
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
