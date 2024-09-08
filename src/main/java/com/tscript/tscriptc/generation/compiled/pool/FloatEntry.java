package com.tscript.tscriptc.generation.compiled.pool;

import com.tscript.tscriptc.generation.writers.PoolEntryWriter;

public class FloatEntry extends BasePoolEntry<Double> {

    protected FloatEntry(int index, Double value) {
        super(index, value);
    }

    @Override
    public PoolTag getTag() {
        return PoolTag.FLOAT;
    }

    @Override
    public void write(PoolEntryWriter writer) {
        writer.writeFloat(this);
    }
}
