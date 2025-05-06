package com.tscript.runtime.jit.ir.types;

public class IRFunctionType implements IRType {

    @Override
    public boolean isCallable() {
        return true;
    }

    @Override
    public <P, R> R accept(IRTypeVisitor<P, R> visitor, P p) {
        return visitor.visitFunctionType(this, p);
    }
}
