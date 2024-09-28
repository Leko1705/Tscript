package com.tscript.compiler.impl.generation.compiled.pool;

import com.tscript.compiler.impl.generation.writers.PoolEntryWriter;

public class FloatEntry extends BasePoolEntry<Double> {

    public FloatEntry(int index, Double value) {
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
