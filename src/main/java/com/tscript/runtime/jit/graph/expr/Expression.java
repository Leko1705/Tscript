package com.tscript.runtime.jit.graph.expr;

public interface Expression {

    Expression EMPTY = new Expression() {
        public <P, R> R accept(ExpressionVisitor<P, R> visitor, P p) { return null; }
    };


    <P, R> R accept(ExpressionVisitor<P, R> visitor, P p);

    default <P, R> R accept(ExpressionVisitor<P, R> visitor) {
        return accept(visitor, null);
    }
}
