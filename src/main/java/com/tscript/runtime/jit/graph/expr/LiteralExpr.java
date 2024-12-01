package com.tscript.runtime.jit.graph.expr;

public abstract class LiteralExpr<V> implements Expression {

    public final V value;

    protected LiteralExpr(V value) {
        this.value = value;
    }

    @Override
    public abstract <P, R> R accept(ExpressionVisitor<P, R> visitor, P p);

}
