package com.tscript.runtime.jit.ir.nodes;

import com.tscript.runtime.jit.ir.IRNode;
import com.tscript.runtime.jit.ir.IRVisitor;
import com.tscript.runtime.jit.ir.types.IRType;
import com.tscript.runtime.jit.ir.types.IRUnknownType;

public class LoadStatic implements IRValue {

    public final String name;

    public IRType type = IRUnknownType.INSTANCE;

    public LoadStatic(String name) {
        this.name = name;
    }

    @Override
    public <P, R> R accept(IRVisitor<P, R> visitor, P p) {
        return visitor.visitLoadStatic(this, p);
    }

    @Override
    public IRType getType() {
        return type;
    }
}
