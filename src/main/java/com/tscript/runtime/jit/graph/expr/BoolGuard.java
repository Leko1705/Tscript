package com.tscript.runtime.jit.graph.expr;

public class BoolGuard extends Guard {

    public boolean required;

    public BoolGuard(Expression expression, boolean required) {
        super(expression);
        this.required = required;
    }

    @Override
    public <P, R> R accept(ExpressionVisitor<P, R> visitor, P p) {
        return visitor.visitBoolGuard(this, p);
    }

}
