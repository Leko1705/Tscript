package com.tscript.runtime.jit.ir.nodes;

import com.tscript.runtime.jit.ir.IRNode;
import com.tscript.runtime.jit.ir.IRVisitor;
import com.tscript.runtime.jit.ir.types.IRFunctionType;
import com.tscript.runtime.jit.ir.types.IRType;

public class Native implements IRValue {

    public final String name;

    public Native(String name) {
        this.name = name;
    }

    @Override
    public <P, R> R accept(IRVisitor<P, R> visitor, P p) {
        return visitor.visitNative(this, p);
    }

    @Override
    public IRType getType() {
        return new IRFunctionType();
    }
}
