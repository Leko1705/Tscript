package com.tscript.tscriptc.generation.compiled.pool;

import com.tscript.tscriptc.generation.writers.PoolEntryWriter;

import java.util.List;

public class DictionaryEntry extends BasePoolEntry<List<Integer>> {

    protected DictionaryEntry(int index, List<Integer> value) {
        super(index, value);
        if (value.size() % 2 != 0) {
            throw new IllegalArgumentException("Dictionary entry should have an even number of elements");
        }
    }

    @Override
    public PoolTag getTag() {
        return PoolTag.DICTIONARY;
    }

    @Override
    public void write(PoolEntryWriter writer) {
        writer.writeDictionary(this);
    }
}
