package com.tscript.runtime.jit.graph.expr;

public class Use implements Expression {

    public String name;
    public Expression used;
    public boolean canFail = true;

    public Use(String name, Expression used) {
        this.name = name;
        this.used = used;
    }

    @Override
    public void replace(Expression toReplace, Expression tree) {
        if (toReplace == used) {
            used = tree;
        }
    }

    @Override
    public <P, R> R accept(ExpressionVisitor<P, R> visitor, P p) {
        return visitor.visitUse(this, p);
    }
}
