package com.tscript.runtime.jit.graph.expr;

public record Native(String name) implements Expression {

    @Override
    public <P, R> R accept(ExpressionVisitor<P, R> visitor, P p) {
        return visitor.visitNative(this, p);
    }
}
