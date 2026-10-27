package com.tscript.runtime.jit.comp.ir.nodes;

import com.tscript.runtime.jit.comp.ir.IRValue;
import com.tscript.runtime.jit.comp.ir.IRValueVisitor;

public class ToMapArg extends UnaryOp {

    private final String name;

    public ToMapArg(IRValue value, String name) {
        super(value);
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public <P, R> R accept(IRValueVisitor<P, R> visitor, P p) {
        return visitor.visitToMapArg(this, p);
    }
}
