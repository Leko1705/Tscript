package com.tscript.runtime.jit.comp.ir.nodes;

import com.tscript.runtime.jit.comp.ir.IRValue;
import com.tscript.runtime.jit.comp.ir.IRValueVisitor;

import java.util.Collections;
import java.util.List;

public class Array extends IRValue {

    private final List<IRValue> values;

    public Array(List<IRValue> values) {
        this.values = values;
        for (IRValue value : values) {
            value.getUsers().add(this);
        }
    }

    public List<IRValue> getValues() {
        return values;
    }

    @Override
    public List<IRValue> getInputs() {
        return getValues();
    }

    @Override
    public <P, R> R accept(IRValueVisitor<P, R> visitor, P p) {
        return visitor.visitArray(this, p);
    }

    @Override
    protected void updateSpecificFields(IRValue oldInput, IRValue newInput) {
        Collections.replaceAll(values, oldInput, newInput);
    }
}
