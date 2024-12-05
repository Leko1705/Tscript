package com.tscript.runtime.jit.graph.expr;

public class ImportModule implements Expression {

    public String name;
    public int index = -1;

    public ImportModule(String name) {
        this.name = name;
    }

    public ImportModule(int index) {
        this.index = index;
    }

    @Override
    public <P, R> R accept(ExpressionVisitor<P, R> visitor, P p) {
        return visitor.visitImportModule(this, p);
    }
}
