package com.tscript.runtime.jit.comp.ir.nodes;

import com.tscript.runtime.jit.comp.ir.IRValueVisitor;

public class Global extends IndexedAccessValue {

    public final boolean mutable;

    public Global(int address, boolean mutable) {
        super(address);
        this.mutable = mutable;
    }

    @Override
    public boolean isConstant() {
        return true;
    }

    @Override
    public <P, R> R accept(IRValueVisitor<P, R> visitor, P p) {
        return visitor.visitGlobal(this, p);
    }

    @Override
    public String toString() {
        return "Global(" + index + ")";
    }
}
