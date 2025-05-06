package com.tscript.runtime.jit.ir.nodes;

import com.tscript.runtime.jit.ir.IRVisitor;
import com.tscript.runtime.jit.ir.types.IRRangeType;
import com.tscript.runtime.jit.ir.types.IRType;

public class Range implements IRValue {

    public IRValue from, to;

    public Range(IRValue from, IRValue to) {
        this.from = from;
        this.to = to;
    }

    @Override
    public <P, R> R accept(IRVisitor<P, R> visitor, P p) {
        return visitor.visitRange(this, p);
    }

    @Override
    public IRType getType() {
        return new IRRangeType();
    }
}
