package com.tscript.runtime.jit.ir.nodes;

import com.tscript.runtime.jit.ir.IRVisitor;
import com.tscript.runtime.jit.ir.types.IRType;

public class ToMapArg implements IRValue {

    public final String name;
    public IRValue value;

    public ToMapArg(String name, IRValue value) {
        this.name = name;
        this.value = value;
    }

    @Override
    public <P, R> R accept(IRVisitor<P, R> visitor, P p) {
        return visitor.visitToMapArg(this, p);
    }

    @Override
    public IRType getType() {
        return value.getType();
    }
}
