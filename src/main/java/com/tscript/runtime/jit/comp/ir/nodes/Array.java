package com.tscript.runtime.jit.comp.ir.nodes;

import com.tscript.runtime.jit.comp.ir.*;
import com.tscript.runtime.jit.comp.optim.CanonicalizerTool;

import java.util.List;

/**
 * Allocates a new array. Allocations are fixed nodes since every evaluation creates a new object.
 */
public class Array extends IRValue {

    public Array(List<IRValue> values) {
        super(values);
    }

    public List<IRValue> getValues() {
        return getInputs();
    }

    @Override
    public boolean isFixed() {
        return true;
    }

    @Override
    public <P, R> R accept(IRValueVisitor<P, R> visitor, P p) {
        return visitor.visitArray(this, p);
    }
}
