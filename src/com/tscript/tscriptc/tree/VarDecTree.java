package com.tscript.tscriptc.tree;

import com.tscript.tscriptc.util.TreeVisitor;

public interface VarDecTree extends DefinitionTree, StatementTree {

    String getName();

    boolean isConstant();

    ExpressionTree getInitializer();

    @Override
    default <P, R> R accept(TreeVisitor<P, R> visitor, P p) {
        return visitor.visitVarDecTree(this, p);
    }
}
