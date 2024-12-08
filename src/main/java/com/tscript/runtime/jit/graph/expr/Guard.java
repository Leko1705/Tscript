package com.tscript.runtime.jit.graph.expr;

public abstract class Guard implements Expression {

    public Expression expression;

    public Guard(Expression expression) {
        this.expression = expression;
    }

    @Override
    public void replace(Expression toReplace, Expression tree) {
        if (toReplace == expression) {
            expression = tree;
        }
    }
}
