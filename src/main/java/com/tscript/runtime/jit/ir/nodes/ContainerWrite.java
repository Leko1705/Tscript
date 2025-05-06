package com.tscript.runtime.jit.ir.nodes;

import com.tscript.runtime.jit.ir.IRNode;
import com.tscript.runtime.jit.ir.IRVisitor;

public class ContainerWrite implements IRNode {

    public IRValue container;
    public IRValue key;
    public IRValue value;

    public ContainerWrite(IRValue container, IRValue key, IRValue value) {
        this.container = container;
        this.key = key;
        this.value = value;
    }

    @Override
    public <P, R> R accept(IRVisitor<P, R> visitor, P p) {
        return visitor.visitContainerWrite(this, p);
    }

    @Override
    public boolean replace(IRValue oldNode, IRValue newNode) {
        if (oldNode == container) {
            container = newNode;
            return true;
        }
        if (oldNode == key) {
            key = newNode;
            return true;
        }
        if (value == oldNode) {
            value = newNode;
            return true;
        }
        return false;
    }
}
