package com.tscript.runtime.jit.comp.ir.nodes;

import com.tscript.runtime.jit.comp.ir.IRValue;
import com.tscript.runtime.jit.comp.ir.IRValueVisitor;

public class Shl extends BinaryOp {

    public Shl(IRValue left, IRValue right) {
        super(left, right);
    }

    @Override
    public <P, R> R accept(IRValueVisitor<P, R> visitor, P p) {
        return visitor.visitShl(this, p);
    }

    @Override
    public String toString() {
        return "(" + getLeft().toString() + " << " + getRight().toString() + ")";
    }
}
