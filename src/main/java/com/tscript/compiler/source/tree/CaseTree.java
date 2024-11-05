package com.tscript.compiler.source.tree;

import com.tscript.compiler.source.utils.TreeVisitor;

public interface CaseTree extends Tree {

    ExpressionTree getExpression();

    StatementTree getStatement();

    boolean allowBreak();

    @Override
    default <P, R> R accept(TreeVisitor<P, R> visitor, P p) {
        return visitor.visitCase(this, p);
    }
}
