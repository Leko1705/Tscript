package com.tscript.runtime.jit.comp.ir.nodes;

import com.tscript.runtime.jit.comp.ir.IRValue;
import com.tscript.runtime.jit.comp.ir.IRValueVisitor;

public class Ushr extends BinaryOp {

    public Ushr(IRValue left, IRValue right) {
        super(left, right);
    }

    @Override
    public <P, R> R accept(IRValueVisitor<P, R> visitor, P p) {
        return visitor.visitUshr(this, p);
    }

    @Override
    public String toString() {
        return "(" + getLeft().toString() + " >>> " + getRight().toString() + ")";
    }
}
