package com.tscript.runtime.jit.graph.expr;

import java.util.List;

public class CallInp implements Expression {

    public Expression called;
    public final List<Expression> args;
    public boolean canFail = true;

    public CallInp(Expression called, List<Expression> args) {
        this.called = called;
        this.args = args;
    }

    @Override
    public <P, R> R accept(ExpressionVisitor<P, R> visitor, P p) {
        return visitor.visitCallInp(this, p);
    }
}
