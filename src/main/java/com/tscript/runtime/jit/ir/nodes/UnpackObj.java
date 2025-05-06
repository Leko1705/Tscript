package com.tscript.runtime.jit.ir.nodes;

import com.tscript.runtime.jit.ir.IRVisitor;
import com.tscript.runtime.jit.ir.types.IRPrimitiveType;
import com.tscript.runtime.jit.ir.types.IRType;
import com.tscript.runtime.jit.ir.types.IRTypeDecorator;

public class UnpackObj implements IRValue {

    public IRValue value;

    public UnpackObj(IRValue value) {
        this.value = value;
    }

    @Override
    public IRType getType() {
        return new IRTypeDecorator(value.getType()) {
            @Override
            public boolean isPrimitive() {
                return value.getType() instanceof IRPrimitiveType;
            }
        };
    }

    @Override
    public <P, R> R accept(IRVisitor<P, R> visitor, P p) {
        return visitor.visitUnpackObj(this, p);
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
