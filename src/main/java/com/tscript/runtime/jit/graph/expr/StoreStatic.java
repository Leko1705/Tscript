package com.tscript.runtime.jit.graph.expr;

public class StoreStatic implements Expression {

    public final String name;
    public Expression what;

    public StoreStatic(String name, Expression what) {
        this.name = name;
        this.what = what;
    }

    @Override
    public <P, R> R accept(ExpressionVisitor<P, R> visitor, P p) {
        return visitor.visitStoreStatic(this, p);
    }
}
