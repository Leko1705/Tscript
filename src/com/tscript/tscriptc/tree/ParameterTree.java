package com.tscript.tscriptc.tree;

import com.tscript.tscriptc.util.TreeVisitor;

public interface ParameterTree extends Tree {

    String getName();

    boolean isConstant();

    ExpressionTree getInitializer();

    @Override
    default <P, R> R accept(TreeVisitor<P, R> visitor, P p) {
        return visitor.visitParameterTree(this, p);
    }
}
