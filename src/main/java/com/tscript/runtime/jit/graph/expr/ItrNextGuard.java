package com.tscript.runtime.jit.graph.expr;

public class ItrNextGuard extends Guard {

    public boolean shouldHaveNext;

    public ItrNextGuard(Expression expression, boolean shouldHaveNext) {
        super(expression);
        this.shouldHaveNext = shouldHaveNext;
    }

    @Override
    public <P, R> R accept(ExpressionVisitor<P, R> visitor, P p) {
        return visitor.visitItrNextGuard(this, p);
    }
}
