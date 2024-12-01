package com.tscript.runtime.jit.graph.expr;

public class Bool extends LiteralExpr<Boolean> {

    public Bool(Boolean value) {
        super(value);
    }

    @Override
    public <P, R> R accept(ExpressionVisitor<P, R> visitor, P p) {
        return visitor.visitBool(this, p);
    }
}
