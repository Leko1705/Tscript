package com.tscript.runtime.jit.ir.nodes;

import com.tscript.runtime.jit.ir.IRNode;
import com.tscript.runtime.jit.ir.IRVisitor;

public class Equals extends Binary {

    public boolean shouldBeEqual;

    public Equals(IRNode lhs, IRNode rhs, boolean shouldBeEqual) {
        super(lhs, rhs);
        this.shouldBeEqual = shouldBeEqual;
    }

    @Override
    public <P, R> R accept(IRVisitor<P, R> visitor, P p) {
        return null;
    }
}
