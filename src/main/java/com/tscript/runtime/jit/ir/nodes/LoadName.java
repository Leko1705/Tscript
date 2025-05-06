package com.tscript.runtime.jit.ir.nodes;

import com.tscript.runtime.jit.ir.IRVisitor;
import com.tscript.runtime.jit.ir.types.IRType;
import com.tscript.runtime.jit.ir.types.IRUnknownType;

public class LoadName implements IRValue {

    public final String name;

    public LoadName(String name) {
        this.name = name;
    }

    @Override
    public <P, R> R accept(IRVisitor<P, R> visitor, P p) {
        return visitor.visitLoadName(this, p);
    }

    @Override
    public IRType getType() {
        return IRUnknownType.INSTANCE;
    }
}
