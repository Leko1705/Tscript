package com.tscript.runtime.jit.comp.ir.nodes;

import com.tscript.runtime.jit.comp.ir.IRValueVisitor;
import com.tscript.runtime.jit.comp.ir.stamps.IntegerStamp;

public class Int extends Literal<Integer> {

    public Int(Integer value) {
        super(value);
        stamp = new IntegerStamp(value, value);
    }

    @Override
    public <P, R> R accept(IRValueVisitor<P, R> visitor, P p) {
        return visitor.visitInteger(this, p);
    }
}
