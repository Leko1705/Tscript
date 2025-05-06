package com.tscript.runtime.jit.ir.nodes;

import com.tscript.runtime.jit.ir.IRVisitor;
import com.tscript.runtime.jit.ir.types.IRType;
import com.tscript.runtime.jit.ir.types.IRUnknownType;

public class LoadLocal implements IRValue {

    public int ssaAddress;
    public final int address;

    public IRType type = IRUnknownType.INSTANCE;

    public LoadLocal(int ssaAddress, int address) {
        this.ssaAddress = ssaAddress;
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
