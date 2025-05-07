package com.tscript.runtime.jit.ir.types;

public class WrappedType implements IRType {

    private final IRType wrappedType;

    public WrappedType(IRType wrappedType) {
        this.wrappedType = wrappedType;
    }

    @Override
    public boolean isIterable() {
        return wrappedType.isIterable();
    }

    @Override
    public boolean isCallable() {
        return wrappedType.isCallable();
    }

    @Override
    public <P, R> R accept(IRTypeVisitor<P, R> visitor, P p) {
        return wrappedType.accept(visitor, p);
    }
}
