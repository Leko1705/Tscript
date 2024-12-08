package com.tscript.runtime.jit.graph.expr;

public abstract class Binary implements Expression {

    public Expression lhs;
    public Expression rhs;

    public Binary(Expression lhs, Expression rhs) {
        this.lhs = lhs;
        this.rhs = rhs;
    }

    @Override
    public void replace(Expression toReplace, Expression tree) {
        if (toReplace == lhs) {
            lhs = tree;
        }
        if (toReplace == rhs) {
            rhs = tree;
        }
    }
}
