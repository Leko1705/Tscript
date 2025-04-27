package com.tscript.runtime.jit.graph.expr;

import java.util.List;

public class CallImage implements Expression {

    public final Expression function;
    public final List<Expression> args;
    public boolean canFail = true;

    public CallImage(Expression called, List<Expression> args) {
        this.function = called;
        this.args = args;
    }

    @Override
    public <P, R> R accept(ExpressionVisitor<P, R> visitor, P p) {
        return visitor.visitCallImage(this, p);
    }
}
