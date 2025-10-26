package com.tscript.runtime.jit.ir.nodes;

import com.tscript.runtime.jit.ir.IRVisitor;
import com.tscript.runtime.jit.ir.types.IRType;
import com.tscript.runtime.jit.ir.types.IRTypeType;

public class Type implements IRValue {

    public int address;

    public Type(int address) {
        this.address = address;
    }

    @Override
    public boolean canBePrimitive() {
        return true;
    }

    @Override
    public <P, R> R accept(IRVisitor<P, R> visitor, P p) {
        return visitor.visitType(this, p);
    }

    @Override
    public IRType getType() {
        return IRTypeType.TYPE_TYPE;
    }
}
