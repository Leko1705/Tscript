package com.tscript.runtime.jit.ir.nodes;

import com.tscript.runtime.jit.ir.IRVisitor;
import com.tscript.runtime.jit.ir.types.IRBooleanType;
import com.tscript.runtime.jit.ir.types.IRType;

public class ItrHasNext implements IRValue {

    public boolean shouldHaveNext;

    public final IRBooleanType type = new IRBooleanType();

    public ItrHasNext(boolean shouldHaveNext) {
        this.shouldHaveNext = shouldHaveNext;
    }

    @Override
    public <P, R> R accept(IRVisitor<P, R> visitor, P p) {
        return visitor.visitItrHasNext(this, p);
    }

    @Override
    public IRType getType() {
        return type;
    }
}
