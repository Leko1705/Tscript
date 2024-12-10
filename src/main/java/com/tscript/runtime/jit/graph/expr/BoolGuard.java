package com.tscript.runtime.jit.graph.expr;

public class BoolGuard extends Guard {

    public boolean required;

    public BoolGuard(Expression expression, boolean required, int recoverAddress, int spRecover) {
        super(expression, recoverAddress, spRecover);
        this.required = required;
    }

    @Override
    public <P, R> R accept(ExpressionVisitor<P, R> visitor, P p) {
        return visitor.visitBoolGuard(this, p);
    }

}
