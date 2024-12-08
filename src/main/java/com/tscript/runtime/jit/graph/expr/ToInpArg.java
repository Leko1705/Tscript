package com.tscript.runtime.jit.graph.expr;

public class ToInpArg implements Expression {

    public Expression arg;

    public ToInpArg(Expression arg) {
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
        return visitor.visitToInpArg(this, p);
    }
}
