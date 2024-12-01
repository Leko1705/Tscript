package com.tscript.runtime.jit.graph.expr;

public class Itr implements Expression {

    public Expression iterable;

    public Itr(Expression iterable) {
        this.iterable = iterable;
    }

    @Override
    public <P, R> R accept(ExpressionVisitor<P, R> visitor, P p) {
        return visitor.visitItr(this, p);
    }
}
