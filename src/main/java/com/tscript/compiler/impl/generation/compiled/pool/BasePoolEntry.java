package com.tscript.compiler.impl.generation.compiled.pool;

import com.tscript.compiler.impl.generation.writers.PoolEntryWriter;

public abstract class BasePoolEntry<T> implements PoolEntry<T> {

    private final int index;
    private final T value;

    protected BasePoolEntry(int index, T value) {
        this.index = index;
        this.value = value;
    }

    @Override
    public int getIndex() {
        return index;
    }


    @Override
    public T get() {
        return value;
    }

    public abstract PoolTag getTag();

    public abstract void write(PoolEntryWriter writer);
}
