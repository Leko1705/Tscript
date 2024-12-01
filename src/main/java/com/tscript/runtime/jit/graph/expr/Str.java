package com.tscript.runtime.jit.graph.expr;

public class Str extends LiteralExpr<String> {

    public Str(String value) {
        super(value);
    }

    @Override
    public <P, R> R accept(ExpressionVisitor<P, R> visitor, P p) {
        return visitor.visitString(this, p);
    }
}
