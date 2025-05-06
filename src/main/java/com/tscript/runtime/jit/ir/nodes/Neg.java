package com.tscript.runtime.jit.ir.nodes;

import com.tscript.runtime.jit.ir.IRVisitor;
import com.tscript.runtime.jit.ir.types.IRType;

public class Neg implements IRValue {

    public IRValue value;

    public Neg(IRValue value) {
        this.value = value;
    }

    @Override
    public <P, R> R accept(IRVisitor<P, R> visitor, P p) {
        return visitor.visitNeg(this, p);
    }

    @Override
    public IRType getType() {
        return value.getType();
    }
}
