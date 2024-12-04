package com.tscript.runtime.jit.graph.expr;

public class PopFrame implements Expression {

    public static final PopFrame INSTANCE = new PopFrame();

    private PopFrame() {}

    @Override
    public <P, R> R accept(ExpressionVisitor<P, R> visitor, P p) {
        return visitor.visitPopFrame(this, p);
    }
}
