package com.tscript.runtime.jit.ir.types;

public class IRRealType implements IRType {

    @Override
    public <P, R> R accept(IRTypeVisitor<P, R> visitor, P p) {
        return visitor.visitRealType(this, p);
    }
}
