package com.tscript.runtime.jit.graph.expr;

public class Unknown implements Expression {

    public static final Unknown INSTANCE = new Unknown();

    private Unknown() {}

    @Override
    public <P, R> R accept(ExpressionVisitor<P, R> visitor, P p) {
        return visitor.visitUnknownValue(this, p);
    }
}
