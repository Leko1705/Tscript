package com.tscript.runtime.jit.ir.nodes;

import com.tscript.runtime.jit.ir.IRNode;
import com.tscript.runtime.jit.ir.IRVisitor;

public class ContainerWrite implements IRNode {

    public IRNode container;
    public IRNode key;
    public IRNode value;

    public ContainerWrite(IRNode container, IRNode key, IRNode value) {
        this.container = container;
        this.key = key;
        this.value = value;
    }

    @Override
    public <P, R> R accept(IRVisitor<P, R> visitor, P p) {
        return null;
    }

}
