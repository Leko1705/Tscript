package com.tscript.runtime.jit.graph.expr;

public record Virtual(int index) implements Expression {

    @Override
    public <P, R> R accept(ExpressionVisitor<P, R> visitor, P p) {
        return visitor.visitVirtual(this, p);
    }

}
