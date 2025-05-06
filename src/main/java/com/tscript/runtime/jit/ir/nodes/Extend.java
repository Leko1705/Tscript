package com.tscript.runtime.jit.ir.nodes;

import com.tscript.runtime.jit.ir.IRNode;
import com.tscript.runtime.jit.ir.IRVisitor;

public class Extend implements IRNode {

    public IRValue owner;
    public String name;
    public IRValue value;

    public Extend(IRValue owner, String name, IRValue value) {
        this.owner = owner;
        this.name = name;
        this.value = value;
    }

    @Override
    public <P, R> R accept(IRVisitor<P, R> visitor, P p) {
        return visitor.visitExtend(this, p);
    }

    @Override
    public boolean replace(IRValue oldNode, IRValue newNode) {
        if (owner == oldNode) {
            owner = newNode;
            return true;
        }
        if (value == oldNode) {
            value = newNode;
            return true;
        }
        return false;
    }
}
