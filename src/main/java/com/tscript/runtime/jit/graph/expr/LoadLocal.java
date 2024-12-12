package com.tscript.runtime.jit.graph.expr;

public class LoadLocal implements Expression {

    public int index;
    public Class<?> primitiveType = null;

    public LoadLocal(int index) {
        this.index = index;
    }

    public LoadLocal(int index, Class<?> primitiveType) {
        this.index = index;
        this.primitiveType = primitiveType;
    }

    @Override
    public <P, R> R accept(ExpressionVisitor<P, R> visitor, P p) {
        return visitor.visitLoadLocal(this, p);
    }
}
