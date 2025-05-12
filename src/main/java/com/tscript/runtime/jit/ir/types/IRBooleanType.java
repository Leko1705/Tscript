package com.tscript.runtime.jit.ir.types;

public class IRBooleanType implements IRType {

    @Override
    public <P, R> R accept(IRTypeVisitor<P, R> visitor, P p) {
        return visitor.visitBooleanType(this, p);
    }
}
