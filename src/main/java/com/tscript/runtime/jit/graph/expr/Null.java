package com.tscript.runtime.jit.graph.expr;

public class Null implements Expression {

    public static final Null INSTANCE = new Null();

    private Null() {}

    @Override
    public <P, R> R accept(ExpressionVisitor<P, R> visitor, P p) {
        return visitor.visitNull(this, p);
    }
}
