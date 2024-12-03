package com.tscript.runtime.jit.graph.expr;

public class StoreExternal implements Expression {

    public final String name;
    public int index = -1;
    public int layer = -1;
    public boolean isStaticAccess = false;
    public Expression where;
    public Expression what;

    public StoreExternal(String name, Expression where, Expression what) {
        this.name = name;
        this.where = where;
        this.what = what;
    }

    @Override
    public <P, R> R accept(ExpressionVisitor<P, R> visitor, P p) {
        return visitor.visitStoreExternal(this, p);
    }
}
