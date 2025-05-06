package com.tscript.runtime.jit.ir.types;

public class IRUnknownType implements IRType {

    public static final IRUnknownType INSTANCE = new IRUnknownType();

    private IRUnknownType() {
    }

    @Override
    public boolean isCallable() {
        return true;
    }

    @Override
    public boolean isIterable() {
        return true;
    }

    @Override
    public <P, R> R accept(IRTypeVisitor<P, R> visitor, P p) {
        return visitor.visitUnknownType(this, p);
    }
}
