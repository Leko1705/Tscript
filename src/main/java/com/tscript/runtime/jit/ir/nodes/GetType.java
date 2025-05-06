package com.tscript.runtime.jit.ir.nodes;

import com.tscript.runtime.jit.ir.IRVisitor;
import com.tscript.runtime.jit.ir.types.IRType;
import com.tscript.runtime.jit.ir.types.IRTypeType;

public class GetType implements IRValue {

    public IRValue value;

    public GetType(IRValue value) {
        this.value = value;
    }

    @Override
    public <P, R> R accept(IRVisitor<P, R> visitor, P p) {
        return visitor.visitGetType(this, p);
    }

    @Override
    public IRType getType() {
        return new IRTypeType(value.getType(), false);
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
