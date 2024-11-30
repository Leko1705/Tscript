package com.tscript.compiler.impl.generation.compiled.pool;

import com.tscript.compiler.impl.generation.writers.PoolEntryWriter;

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
