package com.tscript.runtime.jit.graph.expr;

public class Int extends LiteralExpr<Integer> {

    public boolean asPrimitive = false;

    public Int(Integer value) {
        super(value);
    }

    @Override
    public <P, R> R accept(ExpressionVisitor<P, R> visitor, P p) {
        return visitor.visitInt(this, p);
    }
}
