package com.tscript.runtime.jit.graph.expr;

public class Real extends LiteralExpr<Double> {

    public Real(Double value) {
        super(value);
    }

    @Override
    public <P, R> R accept(ExpressionVisitor<P, R> visitor, P p) {
        return visitor.visitReal(this, p);
    }
}
