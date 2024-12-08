package com.tscript.runtime.jit.graph.expr;

public class PushFrame implements Expression {

    public Expression called;
    public int extraLocals = 0;

    public PushFrame(Expression called) {
        this.called = called;
    }

    @Override
    public void replace(Expression toReplace, Expression tree) {
        if (toReplace == called) {
            called = tree;
        }
    }

    @Override
    public <P, R> R accept(ExpressionVisitor<P, R> visitor, P p) {
        return visitor.visitPushFrame(this, p);
    }
}
