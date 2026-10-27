package com.tscript.runtime.jit.comp.ir.nodes;

import com.tscript.runtime.jit.comp.ir.IRValue;
import com.tscript.runtime.jit.comp.ir.IRValueVisitor;

import java.util.List;

public class Parameter extends IRValue {

    public final int index;
    public Parameter(int index) {
        this.index = index;
    }

    @Override
    public List<IRValue> getInputs() {
        return List.of();
    }

    @Override
    public boolean isConstant() {
        return true;
    }

    @Override
    public <P, R> R accept(IRValueVisitor<P, R> visitor, P p) {
        return visitor.visitParameter(this, p);
    }

    @Override
    public String toString() {
        return "Parameter(" + index + ")";
    }
}
