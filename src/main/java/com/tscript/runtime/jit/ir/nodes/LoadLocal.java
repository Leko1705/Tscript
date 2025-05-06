package com.tscript.runtime.jit.ir.nodes;

import com.tscript.runtime.jit.ir.IRNode;
import com.tscript.runtime.jit.ir.IRVisitor;
import com.tscript.runtime.jit.ir.types.IRType;
import com.tscript.runtime.jit.ir.types.IRUnknownType;

public class LoadLocal implements IRValue {

    public final int address;

    public IRType type = IRUnknownType.INSTANCE;

    public LoadLocal(int address) {
        this.address = address;
    }

    @Override
    public <P, R> R accept(IRVisitor<P, R> visitor, P p) {
        return visitor.visitLoadLocal(this, p);
    }

    @Override
    public IRType getType() {
        return type;
    }
}
