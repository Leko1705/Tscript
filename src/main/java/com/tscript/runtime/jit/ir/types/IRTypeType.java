package com.tscript.runtime.jit.ir.types;

public class IRTypeType implements IRType {

    public static final IRType TYPE_TYPE = new IRTypeType();

    public IRType type;

    public IRTypeType(IRType type, boolean primitive) {
        this.type = type;
    }

    private IRTypeType() {
        this.type = this;
    }

    @Override
    public boolean isCallable() {
        return true;
    }


    @Override
    public <P, R> R accept(IRTypeVisitor<P, R> visitor, P p) {
        return visitor.visitTypeType(this, p);
    }
}
