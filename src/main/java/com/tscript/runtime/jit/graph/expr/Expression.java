package com.tscript.runtime.jit.graph.expr;

public interface Expression {

    <P, R> R accept(ExpressionVisitor<P, R> visitor, P p);

    default <P, R> R accept(ExpressionVisitor<P, R> visitor) {
        return accept(visitor, null);
    }
}
