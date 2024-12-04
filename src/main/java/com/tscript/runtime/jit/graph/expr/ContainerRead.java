package com.tscript.runtime.jit.graph.expr;

public class ContainerRead implements Expression {

    public Expression container, key;
    public boolean canFail = true;

    public ContainerRead(Expression container, Expression key) {
        this.container = container;
        this.key = key;
    }

    @Override
    public <P, R> R accept(ExpressionVisitor<P, R> visitor, P p) {
        return visitor.visitContainerRead(this, p);
    }
}
