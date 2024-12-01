package com.tscript.runtime.jit.graph.expr;

public class StoreGlobal implements Expression {

    public final int index;
    public Expression what;

    public StoreGlobal(int index, Expression what) {
        this.index = index;
        this.what = what;
    }

    @Override
    public <P, R> R accept(ExpressionVisitor<P, R> visitor, P p) {
        return visitor.visitStoreGlobal(this, p);
    }
}
