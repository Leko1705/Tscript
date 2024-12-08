package com.tscript.runtime.jit.graph.expr;

public class StoreTemp implements Expression {

    public Expression what;
    public int index;

    public StoreTemp(Expression what, int index) {
        this.what = what;
        this.index = index;
    }

    @Override
    public <P, R> R accept(ExpressionVisitor<P, R> visitor, P p) {
        return visitor.visitStoreTemp(this, p);
    }
}
