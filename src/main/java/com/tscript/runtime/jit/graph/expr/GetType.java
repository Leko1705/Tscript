package com.tscript.runtime.jit.graph.expr;

public class GetType implements Expression {

    public Expression expression;

    public GetType(Expression expression) {
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
        return visitor.visitGetType(this, p);
    }
}
