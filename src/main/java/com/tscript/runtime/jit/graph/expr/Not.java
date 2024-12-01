package com.tscript.runtime.jit.graph.expr;

public record Not(Expression expression) implements Expression {
    @Override
    public <P, R> R accept(ExpressionVisitor<P, R> visitor, P p) {
        return visitor.visitNot(this, p);
    }
}
