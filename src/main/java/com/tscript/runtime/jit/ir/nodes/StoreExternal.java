package com.tscript.runtime.jit.ir.nodes;

import com.tscript.runtime.jit.ir.IRNode;
import com.tscript.runtime.jit.ir.IRVisitor;

public class StoreExternal implements IRNode {

    public final String name;
    public IRValue owner;
    public IRValue value;

    public StoreExternal(String name, IRValue owner, IRValue value) {
        this.name = name;
        this.owner = owner;
        this.value = value;
    }

    @Override
    public <P, R> R accept(IRVisitor<P, R> visitor, P p) {
        return visitor.visitStoreExternal(this, p);
    }
}
