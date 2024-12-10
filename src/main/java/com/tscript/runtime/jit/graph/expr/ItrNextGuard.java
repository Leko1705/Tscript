package com.tscript.runtime.jit.graph.expr;

public class ItrNextGuard implements Expression {

    public final int ip;
    public final int sp;
    public boolean shouldHaveNext;

    public ItrNextGuard(boolean shouldHaveNext, int ip, int sp) {
        this.shouldHaveNext = shouldHaveNext;
        this.ip = ip;
        this.sp = sp;
    }

    @Override
    public <P, R> R accept(ExpressionVisitor<P, R> visitor, P p) {
        return visitor.visitItrNextGuard(this, p);
    }
}
