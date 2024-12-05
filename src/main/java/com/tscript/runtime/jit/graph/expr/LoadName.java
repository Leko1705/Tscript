package com.tscript.runtime.jit.graph.expr;

public record LoadName(String name, boolean canFail) implements Expression {

    public LoadName(String name){
        this(name, true);
    }

    @Override
    public <P, R> R accept(ExpressionVisitor<P, R> visitor, P p) {
        return visitor.visitLoadName(this, p);
    }
}
