package com.tscript.runtime.jit.ir.nodes;

import com.tscript.runtime.jit.ir.IRNode;
import com.tscript.runtime.jit.ir.IRVisitor;

public class SetOwner implements IRNode {

    public IRNode owner;
    public IRNode member;

    public SetOwner(IRNode owner, IRNode member) {
        this.owner = owner;
        this.member = member;
    }

    @Override
    public <P, R> R accept(IRVisitor<P, R> visitor, P p) {
        return null;
    }
}
