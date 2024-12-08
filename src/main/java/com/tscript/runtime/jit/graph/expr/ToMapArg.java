package com.tscript.runtime.jit.graph.expr;

public class ToMapArg implements Expression {

    public final String name;
    public Expression arg;

    public ToMapArg(String name, Expression arg) {
        this.name = name;
        this.arg = arg;
    }

    @Override
    public void replace(Expression toReplace, Expression tree) {
        if (toReplace == arg) {
            arg = tree;
        }
    }

    @Override
    public <P, R> R accept(ExpressionVisitor<P, R> visitor, P p) {
        return visitor.visitToMapArg(this, p);
    }
}
