package com.tscript.tscriptc.generation.compiled.pool;

import com.tscript.tscriptc.generation.writers.PoolEntryWriter;

import java.util.List;

public class RangeEntry extends BasePoolEntry<List<Integer>> {

    public RangeEntry(int index, List<Integer> value) {
        super(index, value);
        if (value.size() != 2)
            throw new IllegalArgumentException("range must refer to two values");
    }

    @Override
    public PoolTag getTag() {
        return PoolTag.RANGE;
    }

    @Override
    public void write(PoolEntryWriter writer) {
        writer.writeRange(this);
    }
}
