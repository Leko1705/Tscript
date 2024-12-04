package com.tscript.runtime.jit.graph.expr;

public class Native implements Expression {

    public final String name;
    public boolean canFail;

    public Native(String name, boolean canFail) {
        this.name = name;
        this.canFail = canFail;
    }

    @Override
    public <P, R> R accept(ExpressionVisitor<P, R> visitor, P p) {
        return visitor.visitNative(this, p);
    }
}
