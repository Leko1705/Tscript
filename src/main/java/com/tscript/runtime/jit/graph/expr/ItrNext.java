package com.tscript.runtime.jit.graph.expr;

public class ItrNext implements Expression {

    public static final ItrNext INSTANCE = new ItrNext();

    private ItrNext() {}

    @Override
    public <P, R> R accept(ExpressionVisitor<P, R> visitor, P p) {
        return visitor.visitItrNext(this, p);
    }
}
