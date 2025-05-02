package com.tscript.runtime.jit.ir.nodes;

import com.tscript.runtime.jit.ir.IRNode;
import com.tscript.runtime.jit.ir.IRVisitor;

public class ContainerRead implements IRNode {

    public IRNode container;
    public IRNode key;

    public ContainerRead(IRNode container, IRNode key) {
        this.container = container;
        this.key = key;
    }

    @Override
    public <P, R> R accept(IRVisitor<P, R> visitor, P p) {
        return null;
    }

}
