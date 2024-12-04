package com.tscript.runtime.jit.graph.expr;

public class PushFrame implements Expression {

    public Expression called;
    public int extraLocals = 0;

    public PushFrame(Expression called) {
        this.called = called;
    }

    @Override
    public <P, R> R accept(ExpressionVisitor<P, R> visitor, P p) {
        return visitor.visitPushFrame(this, p);
    }
}
