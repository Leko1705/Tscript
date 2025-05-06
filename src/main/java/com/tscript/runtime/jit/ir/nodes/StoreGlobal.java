package com.tscript.runtime.jit.ir.nodes;

import com.tscript.runtime.jit.ir.IRNode;
import com.tscript.runtime.jit.ir.IRVisitor;

public class StoreGlobal implements IRNode {

    public final int address;
    public IRNode value;

    public StoreGlobal(int address, IRNode value) {
        this.address = address;
        this.value = value;
    }

    @Override
    public <P, R> R accept(IRVisitor<P, R> visitor, P p) {
        return visitor.visitStoreGlobal(this, p);
    }
}
