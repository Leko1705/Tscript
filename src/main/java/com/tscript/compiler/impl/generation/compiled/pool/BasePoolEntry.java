package com.tscript.compiler.impl.generation.compiled.pool;

import com.tscript.compiler.impl.generation.writers.PoolEntryWriter;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BasePoolEntry<?> that = (BasePoolEntry<?>) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(index, value);
    }
}
