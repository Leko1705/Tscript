package com.tscript.compiler.impl.generation.compiled.pool;

import com.tscript.compiler.impl.generation.writers.PoolEntryWriter;

public interface PoolEntry<T> {

    int getIndex();

    PoolTag getTag();

    T get();

    void write(PoolEntryWriter writer);

}
