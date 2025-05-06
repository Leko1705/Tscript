package com.tscript.runtime.jit.ir.nodes;

import com.tscript.runtime.jit.ir.IRVisitor;
import com.tscript.runtime.jit.ir.types.IRType;

public class ToInpArg implements IRValue {

    public IRValue value;

    public ToInpArg(IRValue value) {
        this.value = value;
    }

    @Override
    public <P, R> R accept(IRVisitor<P, R> visitor, P p) {
        return visitor.visitToInpArg(this, p);
    }

    @Override
    public IRType getType() {
        return value.getType();
    }
}
