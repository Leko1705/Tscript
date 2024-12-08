package com.tscript.runtime.jit.graph.expr;

public class Range implements Expression {

    public Expression from, to;

    public Range(Expression from, Expression to) {
        this.from = from;
        this.to = to;
    }

    @Override
    public void replace(Expression toReplace, Expression tree) {
        if (toReplace == from) {
            from = tree;
        }
        if (toReplace == to) {
            to = tree;
        }
    }

    @Override
    public <P, R> R accept(ExpressionVisitor<P, R> visitor, P p) {
        return visitor.visitRange(this, p);
    }
}
