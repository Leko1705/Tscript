package com.tscript.tscriptc.tree;

import com.tscript.tscriptc.util.TreeVisitor;

public interface BinaryOperationTree extends BinaryExpressionTree {

    ExpressionTree getLeft();

    ExpressionTree getRight();

    Operation getOperation();

    @Override
    default <P, R> R accept(TreeVisitor<P, R> visitor, P p) {
        return visitor.visitOperationTree(this, p);
    }
}
