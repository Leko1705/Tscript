package com.tscript.tscriptc.tree;

import com.tscript.tscriptc.util.TreeVisitor;

public interface RangeTree extends ExpressionTree {

    ExpressionTree getFrom();

    ExpressionTree getTo();

    @Override
    default <P, R> R accept(TreeVisitor<P, R> visitor, P p) {
        return visitor.visitRangeTree(this, p);
    }
}
