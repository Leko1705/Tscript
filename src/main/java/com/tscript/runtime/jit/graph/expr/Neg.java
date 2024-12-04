package com.tscript.runtime.jit.graph.expr;

public class Neg implements Expression {

    public Expression expression;
    public boolean canFail = true;

    public Neg(Expression expression) {
        this.expression = expression;
    }

    @Override
    public <P, R> R accept(ExpressionVisitor<P, R> visitor, P p) {
        return visitor.visitNeg(this, p);
    }
}
