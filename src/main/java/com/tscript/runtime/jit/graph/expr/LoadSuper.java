package com.tscript.runtime.jit.graph.expr;

public class LoadSuper implements Expression {

    public final String name;
    public int index = -1;
    public int layer = -1;

    public LoadSuper(String name) {
        this.name = name;
    }

    @Override
    public <P, R> R accept(ExpressionVisitor<P, R> visitor, P p) {
        return visitor.visitLoadSuper(this, p);
    }
}
