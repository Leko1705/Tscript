package com.tscript.runtime.jit.graph.expr;

public class Method implements Expression {

    public Expression owner;
    public Expression underlying;
    public boolean canFail = true;

    public Method(Expression owner, Expression underlying) {
        this.owner = owner;
        this.underlying = underlying;
    }

    @Override
    public void replace(Expression toReplace, Expression tree) {
        if (toReplace == underlying) {
            underlying = tree;
        }
        if (toReplace == owner) {
            owner = tree;
        }
    }

    @Override
    public <P, R> R accept(ExpressionVisitor<P, R> visitor, P p) {
        return visitor.visitMethod(this, p);
    }
}
