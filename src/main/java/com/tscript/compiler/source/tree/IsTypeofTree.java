package com.tscript.compiler.source.tree;

import com.tscript.compiler.source.utils.TreeVisitor;

public interface IsTypeofTree extends ExpressionTree {

    ExpressionTree getChecked();

    ExpressionTree getType();

    @Override
    default <P, R> R accept(TreeVisitor<P, R> visitor, P p) {
        return visitor.visitIsTypeofTree(this, p);
    }
}
