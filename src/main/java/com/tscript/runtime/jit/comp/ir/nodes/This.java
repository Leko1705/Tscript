package com.tscript.runtime.jit.comp.ir.nodes;

import com.tscript.runtime.jit.comp.ir.IRValue;
import com.tscript.runtime.jit.comp.ir.IRValueVisitor;

public class This extends IRValue {

    @Override
    public boolean isConstant() {
        return true;
    }

    @Override
    public <P, R> R accept(IRValueVisitor<P, R> visitor, P p) {
        return visitor.visitThis(this, p);
    }

    @Override
    public String toString() {
        return "this";
    }
}
