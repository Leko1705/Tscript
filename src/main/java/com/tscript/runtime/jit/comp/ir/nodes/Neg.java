package com.tscript.runtime.jit.comp.ir.nodes;

import com.tscript.runtime.jit.comp.ir.*;
import com.tscript.runtime.jit.comp.optim.CanonicalizerTool;

public class Neg extends UnaryOp {

    public Neg(IRValue value) {
        super(value);
    }

    @Override
    public <P, R> R accept(IRValueVisitor<P, R> visitor, P p) {
        return visitor.visitNeg(this, p);
    }

    @Override
    public IRValue canonicalize(CanonicalizerTool tool) {
        IRValue value = getValue();

        if (value instanceof Int i) {
            return tool.builder().integer(-i.value);
        }
        if (value instanceof Real r) {
            return tool.builder().real(-r.value);
        }
        if (value instanceof Neg n && tool.isNumeric(n.getValue())) {
            return n.getValue();
        }
        return this;
    }
}
