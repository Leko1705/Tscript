package com.tscript.runtime.jit.graph.expr;

public class BinaryOp extends Binary {

    public BinaryOp(Expression lhs, Expression rhs) {
        super(lhs, rhs);
    }

    @Override
    public <P, R> R accept(ExpressionVisitor<P, R> visitor, P p) {
        return visitor.visitBinaryOp(this, p);
    }
}
