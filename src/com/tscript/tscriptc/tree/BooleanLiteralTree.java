package com.tscript.tscriptc.tree;

import com.tscript.tscriptc.util.TreeVisitor;

public interface BooleanLiteralTree extends LiteralTree<Boolean> {

    Boolean get();

    @Override
    default <P, R> R accept(TreeVisitor<P, R> visitor, P p) {
        return visitor.visitBooleanTree(this, p);
    }
}
