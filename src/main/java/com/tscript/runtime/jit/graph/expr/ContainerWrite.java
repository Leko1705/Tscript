package com.tscript.runtime.jit.graph.expr;

public class ContainerWrite implements Expression {

    public Expression container, key, value;

    public ContainerWrite(Expression container, Expression key, Expression value) {
        this.container = container;
        this.key = key;
        this.value = value;
    }

    @Override
    public <P, R> R accept(ExpressionVisitor<P, R> visitor, P p) {
        return visitor.visitContainerWrite(this, p);
    }
}
