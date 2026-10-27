package com.tscript.runtime.jit.comp.ir.nodes;

import com.tscript.runtime.jit.comp.ir.*;
import com.tscript.runtime.jit.comp.optim.CanonicalizerTool;

/**
 * Reads container[key].
 */
public class ContainerRead extends BinaryOp {

    public ContainerRead(IRValue container, IRValue key) {
        super(container, key);
    }

    public IRValue getContainer() {
        return getLeft();
    }

    public IRValue getKey() {
        return getRight();
    }

    @Override
    public boolean isFixed() {
        return true;
    }

    @Override
    public <P, R> R accept(IRValueVisitor<P, R> visitor, P p) {
        return visitor.visitContainerRead(this, p);
    }
}
