package com.tscript.runtime.jit.comp.ir.nodes;

import com.tscript.runtime.jit.comp.ir.IRValueVisitor;

public class Local extends IndexedAccessValue {

    public Local(int address) {
        super(address);
    }

    @Override
    public boolean isConstant() {
        return true;
    }

    @Override
    public <P, R> R accept(IRValueVisitor<P, R> visitor, P p) {
        return visitor.visitLocal(this, p);
    }

    @Override
    public String toString() {
        return "Local(" + index + ")";
    }
}
