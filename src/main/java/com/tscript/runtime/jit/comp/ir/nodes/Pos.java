package com.tscript.runtime.jit.comp.ir.nodes;

import com.tscript.runtime.jit.comp.ir.IRValue;
import com.tscript.runtime.jit.comp.ir.IRValueVisitor;
import com.tscript.runtime.jit.comp.optim.CanonicalizerTool;

public class Pos extends UnaryOp {

    public Pos(IRValue value) {
        super(value);
    }

    @Override
    public <P, R> R accept(IRValueVisitor<P, R> visitor, P p) {
        return visitor.visitPos(this, p);
    }

    @Override
    public String toString() {
        return "+" + getValue().toString();
    }

    @Override
    public IRValue canonicalize(CanonicalizerTool tool) {
        IRValue value = getValue();

        if (tool.isNumeric(value)) {
            return value;
        }

        return this;
    }
}
