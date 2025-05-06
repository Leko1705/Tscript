package com.tscript.runtime.jit.ir.nodes;

import com.tscript.runtime.jit.ir.IRVisitor;
import com.tscript.runtime.jit.ir.types.IRRealType;
import com.tscript.runtime.jit.ir.types.IRType;

public class Real extends Literal<Double> {

    public final IRRealType type = new IRRealType();

    public Real(Double value) {
        super(value);
    }

    @Override
    public <P, R> R accept(IRVisitor<P, R> visitor, P p) {
        return visitor.visitReal(this, p);
    }

    @Override
    public IRType getType() {
        return type;
    }
}
