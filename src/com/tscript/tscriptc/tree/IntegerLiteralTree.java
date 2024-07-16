package com.tscript.tscriptc.tree;

import com.tscript.tscriptc.util.TreeVisitor;

public interface IntegerLiteralTree extends LiteralTree<Integer> {

    Integer get();

    @Override
    default <P, R> R accept(TreeVisitor<P, R> visitor, P p) {
        return visitor.visitIntegerTree(this, p);
    }
}
