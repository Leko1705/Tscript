package com.tscript.runtime.jit.ir.types;

public class IRTypeDecorator implements IRType {

    private final IRType decorated;

    public IRTypeDecorator(IRType decorated) {
        this.decorated = decorated;
    }

    @Override
    public boolean isCallable() {
        return decorated.isCallable();
    }

    @Override
    public boolean isIterable() {
        return decorated.isIterable();
    }

    @Override
    public <P, R> R accept(IRTypeVisitor<P, R> visitor, P p) {
        return decorated.accept(visitor, p);
    }
}
