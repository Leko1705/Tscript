package com.tscript.tscriptc.tree;

import com.tscript.tscriptc.util.TreeVisitor;

public interface MemberAccessTree extends UnaryExpressionTree {

    ExpressionTree getExpression();

    String getMemberName();

    @Override
    default <P, R> R accept(TreeVisitor<P, R> visitor, P p) {
        return visitor.visitMemberAccessTree(this, p);
    }
}
