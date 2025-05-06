package com.tscript.runtime.jit.ir.nodes;

import com.tscript.runtime.jit.ir.IRVisitor;
import com.tscript.runtime.jit.ir.types.IRType;
import com.tscript.runtime.jit.ir.types.IRUnknownType;

public class LoadSuper implements IRValue {

    public final String name;

    public IRType type = IRUnknownType.INSTANCE;

    public LoadSuper(String name) {
        this.name = name;
    }

    @Override
    public <P, R> R accept(IRVisitor<P, R> visitor, P p) {
        return visitor.visitLoadSuper(this, p);
    }

    @Override
    public IRType getType() {
        return type;
    }
}
