package com.tscript.compiler.impl.generation.compiled.pool;

import com.tscript.compiler.impl.generation.writers.PoolEntryWriter;

public class UTF8Entry extends BasePoolEntry<String> {

    public UTF8Entry(int index, String value) {
        super(index, value);
    }

    @Override
    public PoolTag getTag() {
        return PoolTag.UTF8;
    }

    @Override
    public void write(PoolEntryWriter writer) {
        writer.writeUTF8(this);
    }
}
