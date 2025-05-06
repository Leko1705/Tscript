package com.tscript.runtime.jit.ir.types;

public class IRStringType implements IRPrimitiveType {

    @Override
    public boolean isIterable() {
        return true;
    }

    @Override
    public <P, R> R accept(IRTypeVisitor<P, R> visitor, P p) {
        return visitor.visitStringType(this, p);
    }
}
