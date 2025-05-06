package com.tscript.runtime.jit.ir.nodes;

import com.tscript.runtime.jit.ir.IRVisitor;
import com.tscript.runtime.jit.ir.types.IRFunctionType;
import com.tscript.runtime.jit.ir.types.IRType;

public class Virtual implements IRValue {

    public final int address;

    public Virtual(int address) {
        this.address = address;
    }

    @Override
    public <P, R> R accept(IRVisitor<P, R> visitor, P p) {
        return visitor.visitVirtual(this, p);
    }

    @Override
    public IRType getType() {
        return new IRFunctionType();
    }
}
