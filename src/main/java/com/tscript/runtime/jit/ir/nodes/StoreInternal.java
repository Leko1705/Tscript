package com.tscript.runtime.jit.ir.nodes;

import com.tscript.runtime.jit.ir.IRNode;
import com.tscript.runtime.jit.ir.IRVisitor;

public class StoreInternal implements IRNode {

    public final int address;
    public IRValue value;

    public StoreInternal(int address, IRValue value) {
        this.address = address;
        this.value = value;
    }

    @Override
    public <P, R> R accept(IRVisitor<P, R> visitor, P p) {
        return visitor.visitStoreInternal(this, p);
    }
}
