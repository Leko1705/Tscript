package com.tscript.runtime.jit.ir.nodes;

import com.tscript.runtime.jit.ir.IRNode;
import com.tscript.runtime.jit.ir.IRVisitor;

import java.util.Objects;

public class StoreLocal implements IRNode {

    public int ssaAddress;
    public final int address;
    public IRValue value;

    public StoreLocal(int ssaAddress, int address, IRValue value) {
        this.ssaAddress = ssaAddress;
        this.address = address;
        this.value = Objects.requireNonNull(value);
    }

    @Override
    public <P, R> R accept(IRVisitor<P, R> visitor, P p) {
        return visitor.visitStoreLocal(this, p);
    }
}
