package com.tscript.tscriptc.generation.compiled.pool;

import com.tscript.tscriptc.generation.writers.PoolEntryWriter;

public class BooleanEntry extends BasePoolEntry<Boolean> {

    protected BooleanEntry(int index, Boolean value) {
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
