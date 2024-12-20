package com.tscript.compiler.impl.generation.compiled.pool;

import com.tscript.compiler.impl.generation.writers.PoolEntryWriter;

public class StringEntry extends BasePoolEntry<String> {

    public StringEntry(int index, String value) {
        super(index, value);
    }

    @Override
    public PoolTag getTag() {
        return PoolTag.STRING;
    }

    @Override
    public void write(PoolEntryWriter writer) {
        writer.writeString(this);
    }

}
