package com.tscript.runtime.jit.graph.expr;

public class ContainerWrite implements Expression {

    public Expression container, key, value;

    public ContainerWrite(Expression container, Expression key, Expression value) {
        this.container = container;
        this.key = key;
        this.value = value;
    }

    @Override
    public void replace(Expression toReplace, Expression tree) {
        if (container == toReplace) {
            container = tree;
        }
        if (key == toReplace) {
            key = tree;
        }
        if (value == toReplace) {
            value = tree;
        }
    }

    @Override
    public <P, R> R accept(ExpressionVisitor<P, R> visitor, P p) {
        return visitor.visitContainerWrite(this, p);
    }
}
