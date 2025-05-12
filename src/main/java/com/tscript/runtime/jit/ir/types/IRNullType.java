package com.tscript.runtime.jit.ir.types;

public class IRNullType implements IRType {

    @Override
    public <P, R> R accept(IRTypeVisitor<P, R> visitor, P p) {
        return visitor.visitNullType(this, p);
    }
}
