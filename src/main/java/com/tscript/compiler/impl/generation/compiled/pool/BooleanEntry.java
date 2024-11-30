package com.tscript.compiler.impl.generation.compiled.pool;

import com.tscript.compiler.impl.generation.writers.PoolEntryWriter;

public class BooleanEntry extends BasePoolEntry<Boolean> {

    public BooleanEntry(int index, Boolean value) {
        super(index, value);
    }

    @Override
    public PoolTag getTag() {
        return PoolTag.BOOL;
    }

    @Override
    public void write(PoolEntryWriter writer) {
        writer.writeBoolean(this);
    }
}
