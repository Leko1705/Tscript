package com.tscript.runtime.jit.graph.expr;

public class StoreTemp implements Expression {

    public Expression what;
    public int index;
    public Class<?> primitiveType = null;

    public StoreTemp(Expression what, int index) {
        this.what = what;
        this.index = index;
    }

    @Override
    public void replace(Expression toReplace, Expression tree) {
        if (toReplace == what) {
            what = tree;
        }
    }

    @Override
    public <P, R> R accept(ExpressionVisitor<P, R> visitor, P p) {
        return visitor.visitStoreTemp(this, p);
    }
}
