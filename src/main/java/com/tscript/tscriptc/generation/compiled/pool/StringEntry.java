package com.tscript.tscriptc.generation.compiled.pool;

import com.tscript.tscriptc.generation.writers.PoolEntryWriter;

public class StringEntry extends BasePoolEntry<String> {

    protected StringEntry(int index, String value) {
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
