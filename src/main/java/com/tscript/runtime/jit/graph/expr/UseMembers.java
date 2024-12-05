package com.tscript.runtime.jit.graph.expr;

public class UseMembers implements Expression {

    public Expression used;
    public boolean canFail = true;

    public UseMembers(Expression used) {
        this.used = used;
    }

    @Override
    public <P, R> R accept(ExpressionVisitor<P, R> visitor, P p) {
        return visitor.visitUseMembers(this, p);
    }
}
