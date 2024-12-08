package com.tscript.runtime.jit.graph.expr;

public class LoadTemp implements Expression {

    public int index;

    public LoadTemp(int index) {
        this.index = index;
    }

    @Override
    public <P, R> R accept(ExpressionVisitor<P, R> visitor, P p) {
        return visitor.visitLoadTemp(this, p);
    }
}
