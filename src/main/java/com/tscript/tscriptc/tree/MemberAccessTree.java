package com.tscript.tscriptc.tree;

public interface MemberAccessTree extends ExpressionTree {

    ExpressionTree getExpression();

    String getMemberName();
}
