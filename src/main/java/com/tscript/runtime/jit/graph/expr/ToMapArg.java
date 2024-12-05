package com.tscript.runtime.jit.graph.expr;

public class ToMapArg implements Expression {

    public final String name;
    public Expression arg;

    public ToMapArg(String name, Expression arg) {
        this.name = name;
        this.arg = arg;
    }

    @Override
    public <P, R> R accept(ExpressionVisitor<P, R> visitor, P p) {
        return null;
    }
}
