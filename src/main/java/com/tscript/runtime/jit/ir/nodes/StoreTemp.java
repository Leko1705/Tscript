package com.tscript.runtime.jit.ir.nodes;

import com.tscript.runtime.jit.ir.IRNode;
import com.tscript.runtime.jit.ir.IRVisitor;

public class StoreTemp implements IRNode {

    public final int address;
    public IRValue value;

    public StoreTemp(int address, IRValue value) {
        this.address = address;
        this.value = value;
    }

    @Override
    public boolean replace(IRValue oldNode, IRValue newNode) {
        if (oldNode == value) {
            value = newNode;
            return true;
        }
        return false;
    }

    @Override
    public <P, R> R accept(IRVisitor<P, R> visitor, P p) {
        return visitor.visitStoreTemp(this, p);
    }
}
