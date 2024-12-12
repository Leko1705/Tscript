package com.tscript.runtime.jit.graph.expr;

public class Neg implements Expression {

    public Class<?> primitiveTarget = null;

    public Expression expression;
    public boolean canFail;

    public Neg(Expression expression, boolean canFail) {
        this.expression = expression;
        this.canFail = canFail;
    }

    @Override
    public void replace(Expression toReplace, Expression tree) {
        if (toReplace == expression) {
            expression = tree;
        }
    }

    @Override
    public <P, R> R accept(ExpressionVisitor<P, R> visitor, P p) {
        return visitor.visitNeg(this, p);
    }
}
