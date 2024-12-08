package com.tscript.runtime.jit.graph.expr;

public class Pop implements Expression {

    public Expression expression;

    public Pop(Expression expression) {
        this.expression = expression;
    }

    @Override
    public void replace(Expression toReplace, Expression tree) {
        if (toReplace == expression) {
            expression = tree;
        }
    }

    @Override
    public <P, R> R accept(ExpressionVisitor<P, R> visitor, P p) {
        return visitor.visitPop(this, p);
    }
}
