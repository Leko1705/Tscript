package com.tscript.runtime.jit.ir.nodes;

import com.tscript.runtime.jit.ir.IRVisitor;
import com.tscript.runtime.jit.ir.types.IRType;

public class SetOwner implements IRValue {

    public IRValue owner;
    public IRValue member;

    public SetOwner(IRValue owner, IRValue member) {
        this.owner = owner;
        this.member = member;
    }

    @Override
    public <P, R> R accept(IRVisitor<P, R> visitor, P p) {
        return visitor.visitSetOwner(this, p);
    }

    @Override
    public IRType getType() {
        return owner.getType();
    }
}
