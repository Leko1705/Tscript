package com.tscript.runtime.jit.graph.expr;

public class ToInpArg implements Expression {

    public Expression arg;

    public ToInpArg(Expression arg) {
        this.arg = arg;
    }

    @Override
    public <P, R> R accept(ExpressionVisitor<P, R> visitor, P p) {
        return null;
    }
}
