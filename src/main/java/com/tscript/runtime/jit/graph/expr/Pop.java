package com.tscript.runtime.jit.graph.expr;

public record Pop(Expression expression) implements Expression {
    @Override
    public <P, R> R accept(ExpressionVisitor<P, R> visitor, P p) {
        return visitor.visitPop(this, p);
    }
}
