package com.tscript.runtime.jit.comp.ir.nodes;

import com.tscript.runtime.jit.comp.ir.IRValueVisitor;
import com.tscript.runtime.jit.comp.ir.stamps.RealStamp;

public class Real extends Literal<Double> {

    public Real(Double value) {
        super(value);
        stamp = new RealStamp(value, value);
    }

    @Override
    public <P, R> R accept(IRValueVisitor<P, R> visitor, P p) {
        return visitor.visitReal(this, p);
    }
}
