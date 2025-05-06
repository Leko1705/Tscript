package com.tscript.runtime.jit.ir.nodes;

import com.tscript.runtime.jit.ir.IRVisitor;
import com.tscript.runtime.jit.ir.types.IRBooleanType;
import com.tscript.runtime.jit.ir.types.IRType;

public class Equals extends Binary {

    public boolean shouldBeEqual;

    public final IRBooleanType type = new IRBooleanType();

    public Equals(IRValue lhs, IRValue rhs, boolean shouldBeEqual) {
        super(lhs, rhs);
        this.shouldBeEqual = shouldBeEqual;
    }

    @Override
    public <P, R> R accept(IRVisitor<P, R> visitor, P p) {
        return visitor.visitEquals(this, p);
    }

    @Override
    public IRType getType() {
        return type;
    }
}
