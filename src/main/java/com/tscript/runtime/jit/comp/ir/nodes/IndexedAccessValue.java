package com.tscript.runtime.jit.comp.ir.nodes;

import com.tscript.runtime.jit.comp.ir.IRValue;

import java.util.List;

public abstract class IndexedAccessValue extends IRValue {

    public final int index;

    public IndexedAccessValue(int address) {
        this.index = address;
    }

    @Override
    public List<IRValue> getInputs() {
        return List.of();
    }

}
