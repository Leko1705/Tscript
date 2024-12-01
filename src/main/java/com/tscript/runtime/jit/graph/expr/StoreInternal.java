package com.tscript.runtime.jit.graph.expr;

public class StoreInternal implements Expression {

    public final int index;
    public Expression what;

    public StoreInternal(int index, Expression what) {
        this.index = index;
        this.what = what;
    }

    @Override
    public <P, R> R accept(ExpressionVisitor<P, R> visitor, P p) {
        return visitor.visitStoreInternal(this, p);
    }
}
