package com.tscript.runtime.jit.graph.expr;

public class Abstract implements Expression {

    public final String name;
    public int index = -1;
    public int layer = 0;
    public boolean canFail;

    public Abstract(String name, boolean canFail) {
        this.name = name;
        this.canFail = canFail;
    }

    @Override
    public <P, R> R accept(ExpressionVisitor<P, R> visitor, P p) {
        return visitor.visitAbstract(this, p);
    }
}
