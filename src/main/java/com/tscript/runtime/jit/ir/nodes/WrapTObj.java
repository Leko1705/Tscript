package com.tscript.runtime.jit.ir.nodes;

import com.tscript.runtime.jit.ir.IRVisitor;
import com.tscript.runtime.jit.ir.types.IRType;
import com.tscript.runtime.jit.ir.types.IRTypeDecorator;
import com.tscript.runtime.jit.ir.types.WrappedType;

public class WrapTObj implements IRValue {

    public IRValue value;

    public WrapTObj(IRValue value) {
        this.value = value;
    }

    @Override
    public <P, R> R accept(IRVisitor<P, R> visitor, P p) {
        return visitor.visitWrapTObj(this, p);
    }

    @Override
    public IRType getType() {
        return new WrappedType(value.getType());
    }

    @Override
    public boolean replace(IRValue oldNode, IRValue newNode) {
        if (oldNode == value) {
            value = newNode;
            return true;
        }
        return false;
    }
}
