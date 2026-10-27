package com.tscript.runtime.jit.comp.ir.nodes;

import com.tscript.runtime.jit.comp.ir.IRValueVisitor;

public class NullValue extends Literal<Void> {

    public NullValue() {
        super(null);
    }

    @Override
    public <P, R> R accept(IRValueVisitor<P, R> visitor, P p) {
        return  visitor.visitNull(this, p);
    }
}
