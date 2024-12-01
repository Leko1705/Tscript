package com.tscript.runtime.jit.graph.expr;

public record Method(Expression owner, Expression underlying) implements Expression {
    @Override
    public <P, R> R accept(ExpressionVisitor<P, R> visitor, P p) {
        return visitor.visitMethod(this, p);
    }
}
