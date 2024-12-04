package com.tscript.runtime.jit.graph.expr;

public class Not implements Expression {

    public Expression expression;
    public boolean canFail = true;

    public Not(Expression expression) {
        this.expression = expression;
    }

    @Override
    public <P, R> R accept(ExpressionVisitor<P, R> visitor, P p) {
        return visitor.visitNot(this, p);
    }
}
