package com.tscript.runtime.jit.graph.expr;

public class This implements Expression {

    public static final This INSTANCE = new This();

    private This() {}

    @Override
    public <P, R> R accept(ExpressionVisitor<P, R> visitor, P p) {
        return visitor.visitThis(this, p);
    }
}
