package com.tscript.runtime.jit.graph.expr;

public record NewLine(int line) implements Expression {
    @Override
    public <P, R> R accept(ExpressionVisitor<P, R> visitor, P p) {
        return visitor.visitNewLine(this, p);
    }
}
