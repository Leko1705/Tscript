package com.tscript.runtime.jit.graph.expr;

public class StoreSuper implements Expression {

    public final String name;
    public int index = -1;
    public int layer = -1;
    public Expression what;

    public StoreSuper(String name, Expression what) {
        this.name = name;
        this.what = what;
    }

    @Override
    public <P, R> R accept(ExpressionVisitor<P, R> visitor, P p) {
        return visitor.visitStoreSuper(this, p);
    }
}
