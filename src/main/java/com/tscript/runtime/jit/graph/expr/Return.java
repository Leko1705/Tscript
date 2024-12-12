package com.tscript.runtime.jit.graph.expr;

public class Return implements Expression {

    public Class<?> targetType = null;
    public Expression expression;

    public Return(Expression expression) {
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
        return visitor.visitReturn(this, p);
    }
}
