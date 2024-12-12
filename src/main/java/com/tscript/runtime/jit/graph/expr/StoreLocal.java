package com.tscript.runtime.jit.graph.expr;

public class StoreLocal implements Expression {

    public final int index;
    public Expression what;
    public Class<?> primitiveType = null;

    public StoreLocal(int index, Expression what) {
        this.index = index;
        this.what = what;
    }

    public StoreLocal(int index, Expression what, Class<?> primitiveType) {
        this.index = index;
        this.what = what;
        this.primitiveType = primitiveType;
    }

    @Override
    public void replace(Expression toReplace, Expression tree) {
        if (what == toReplace) {
            what = tree;
        }
    }

    @Override
    public <P, R> R accept(ExpressionVisitor<P, R> visitor, P p) {
        return visitor.visitStoreLocal(this, p);
    }
}
