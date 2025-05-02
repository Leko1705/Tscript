package com.tscript.runtime.jit.ir.nodes;

import com.tscript.runtime.jit.ir.IRNode;
import com.tscript.runtime.jit.ir.IRVisitor;

public class Extend implements IRNode {

    public IRNode owner;
    public String name;
    public IRNode value;

    public Extend(IRNode owner, String name, IRNode value) {
        this.owner = owner;
        this.name = name;
        this.value = value;
    }

    @Override
    public <P, R> R accept(IRVisitor<P, R> visitor, P p) {
        return null;
    }
}
