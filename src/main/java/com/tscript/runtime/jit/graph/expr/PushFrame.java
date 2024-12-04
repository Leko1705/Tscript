package com.tscript.runtime.jit.graph.expr;

public class PushFrame implements Expression {

    public int extraLocals = 0;

    @Override
    public <P, R> R accept(ExpressionVisitor<P, R> visitor, P p) {
        return visitor.visitPushFrame(this, p);
    }
}
