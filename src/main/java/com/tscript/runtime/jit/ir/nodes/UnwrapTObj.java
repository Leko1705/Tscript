package com.tscript.runtime.jit.ir.nodes;

import com.tscript.runtime.jit.ir.IRVisitor;
import com.tscript.runtime.jit.ir.types.IRType;

public class UnwrapTObj implements IRValue {

    public IRValue value;

    public UnwrapTObj(IRValue value) {
        this.value = value;
    }

    @Override
    public IRType getType() {
        return value.getType();
    }

    @Override
    public boolean replace(IRValue oldNode, IRValue newNode) {
        if (oldNode == value) {
            value = newNode;
            return true;
        }
        return false;
    }

    @Override
    public <P, R> R accept(IRVisitor<P, R> visitor, P p) {
        return visitor.visitUnwrapTObj(this, p);
    }
}
