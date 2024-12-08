package com.tscript.runtime.jit.graph.expr;

public class Extended implements Expression {

    public Expression owner;
    public String name;
    public Expression content;
    public boolean canFail = true;

    public Extended(Expression owner, String name, Expression content) {
        this.owner = owner;
        this.name = name;
        this.content = content;
    }

    @Override
    public void replace(Expression toReplace, Expression tree) {
        if (owner == tree) {
            owner = toReplace;
        }
        if (content == tree) {
            content = toReplace;
        }
    }

    @Override
    public <P, R> R accept(ExpressionVisitor<P, R> visitor, P p) {
        return visitor.visitExtended(this, p);
    }
}
