package com.tscript.runtime.jit.graph.expr;

public class ItrNextGuard implements Expression {

    public boolean shouldHaveNext;

    public ItrNextGuard(boolean shouldHaveNext) {
        this.shouldHaveNext = shouldHaveNext;
    }

    @Override
    public <P, R> R accept(ExpressionVisitor<P, R> visitor, P p) {
        return visitor.visitItrNextGuard(this, p);
    }
}
