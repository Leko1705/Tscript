package com.tscript.runtime.jit.comp.ir.nodes;

import com.tscript.runtime.jit.comp.ir.IRValueVisitor;
import com.tscript.runtime.jit.comp.ir.stamps.BoolStamp;

public class Bool extends Literal<Boolean> {

    public Bool(Boolean value) {
        super(value);
        stamp = new BoolStamp(value, !value);
    }

    @Override
    public <P, R> R accept(IRValueVisitor<P, R> visitor, P p) {
        return visitor.visitBool(this, p);
    }

}
