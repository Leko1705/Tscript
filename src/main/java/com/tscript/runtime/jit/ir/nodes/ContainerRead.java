package com.tscript.runtime.jit.ir.nodes;

import com.tscript.runtime.jit.ir.IRVisitor;
import com.tscript.runtime.jit.ir.types.IRType;
import com.tscript.runtime.jit.ir.types.IRUnknownType;

public class ContainerRead implements IRValue {

    public IRValue container;
    public IRValue key;
    public IRType type = IRUnknownType.INSTANCE;

    public ContainerRead(IRValue container, IRValue key) {
        this.container = container;
        this.key = key;
    }

    @Override
    public <P, R> R accept(IRVisitor<P, R> visitor, P p) {
        return visitor.visitContainerRead(this, p);
    }

    @Override
    public IRType getType() {
        return type;
    }

    @Override
    public boolean replace(IRValue oldNode, IRValue newNode) {
        if (oldNode == key) {
            key = newNode;
            return true;
        }
        if (oldNode == container) {
            container = newNode;
            return true;
        }
        return false;
    }
}
