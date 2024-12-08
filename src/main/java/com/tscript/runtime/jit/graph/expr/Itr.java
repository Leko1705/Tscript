package com.tscript.runtime.jit.graph.expr;

public class Itr implements Expression {

    public Expression iterable;
    public boolean canFail = true;

    public Itr(Expression iterable) {
        this.iterable = iterable;
    }

    @Override
    public void replace(Expression toReplace, Expression tree) {
        if (toReplace == iterable) {
            iterable = tree;
        }
    }

    @Override
    public <P, R> R accept(ExpressionVisitor<P, R> visitor, P p) {
        return visitor.visitItr(this, p);
    }
}
