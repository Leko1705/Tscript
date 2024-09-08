package com.tscript.tscriptc.generation.compiled.pool;

import com.tscript.tscriptc.generation.writers.PoolEntryWriter;

public interface PoolEntry<T> {

    int getIndex();

    PoolTag getTag();

    T get();

    void write(PoolEntryWriter writer);

}
