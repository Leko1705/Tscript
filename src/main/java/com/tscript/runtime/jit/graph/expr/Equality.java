package com.tscript.runtime.jit.graph.expr;

public class Equality extends Binary {

    public boolean shouldBeEqual;

    public Equality(Expression lhs, Expression rhs, boolean shouldBeEqual) {
        super(lhs, rhs);
        this.shouldBeEqual = shouldBeEqual;
    }

    @Override
    public <P, R> R accept(ExpressionVisitor<P, R> visitor, P p) {
        return visitor.visitEquality(this, p);
    }
}
