package com.tscript.runtime.jit.graph.expr;

public abstract class Guard implements Expression {

    public Expression expression;

    public final int recoverStackPointerAddr;
    public final int recoverAddress;

    public Guard(Expression expression, int recoverAddress, int recoverStackPointerAddr) {
        this.expression = expression;
        this.recoverAddress = recoverAddress;
        this.recoverStackPointerAddr = recoverStackPointerAddr;
    }

    @Override
    public void replace(Expression toReplace, Expression tree) {
        if (toReplace == expression) {
            expression = tree;
        }
    }
}
