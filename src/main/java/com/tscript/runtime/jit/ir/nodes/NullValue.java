package com.tscript.runtime.jit.ir.nodes;

import com.tscript.runtime.jit.ir.IRVisitor;
import com.tscript.runtime.jit.ir.types.IRNullType;
import com.tscript.runtime.jit.ir.types.IRType;

public class NullValue extends Literal<Void> {

    public final IRNullType type = new IRNullType();

    public NullValue() {
        super(null);
    }

    @Override
    public <P, R> R accept(IRVisitor<P, R> visitor, P p) {
        return visitor.visitNull(this, p);
    }

    @Override
    public IRType getType() {
        return type;
    }
}
