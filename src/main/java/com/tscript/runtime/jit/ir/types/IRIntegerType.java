package com.tscript.runtime.jit.ir.types;

public class IRIntegerType implements IRType {

    @Override
    public <P, R> R accept(IRTypeVisitor<P, R> visitor, P p) {
        return visitor.visitIntegerType(this, p);
    }
}
