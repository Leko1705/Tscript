package com.tscript.runtime.jit.graph.expr;

import java.util.List;

public record Array(List<Expression> contents) implements Expression {
    @Override
    public <P, R> R accept(ExpressionVisitor<P, R> visitor, P p) {
        return visitor.visitArray(this, p);
    }

    @Override
    public void replace(Expression toReplace, Expression tree) {
        contents.replaceAll(candidate -> candidate == toReplace ? tree : candidate);
    }
}
