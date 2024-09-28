package com.tscript.compiler.impl.generation.compiled.pool;

import com.tscript.compiler.impl.generation.writers.PoolEntryWriter;

public class IntegerEntry extends BasePoolEntry<Integer> {

    public IntegerEntry(int index, Integer value) {
        super(index, value);
    }

    @Override
    public PoolTag getTag() {
        return PoolTag.INTEGER;
    }

    @Override
    public void write(PoolEntryWriter writer) {
        writer.writeInteger(this);
    }
}
