package com.tscript.runtime.jit.graph.expr;

public class LoadExternal implements Expression {

    public Expression what;
    public final String name;
    public boolean isStaticAccess = false;
    public int index = -1;
    public int layer = -1;

    public LoadExternal(Expression what, String name) {
        this.what = what;
        this.name = name;
    }

    @Override
    public void replace(Expression toReplace, Expression tree) {
        if (what == toReplace) {
            what = tree;
        }
    }

    @Override
    public <P, R> R accept(ExpressionVisitor<P, R> visitor, P p) {
        return visitor.visitLoadExternal(this, p);
    }
}
