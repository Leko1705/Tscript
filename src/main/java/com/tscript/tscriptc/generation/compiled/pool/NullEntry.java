package com.tscript.tscriptc.generation.compiled.pool;

import com.tscript.tscriptc.generation.writers.PoolEntryWriter;

public class NullEntry extends BasePoolEntry<Void> {

    public NullEntry(int index) {
        super(index, null);
    }

    @Override
    public PoolTag getTag() {
        return PoolTag.NULL;
    }

    @Override
    public void write(PoolEntryWriter writer) {
        writer.writeNull(this);
    }
}
