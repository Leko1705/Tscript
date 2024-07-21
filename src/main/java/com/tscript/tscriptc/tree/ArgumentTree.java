package com.tscript.tscriptc.tree;

public interface ArgumentTree extends Tree {

    String getName();

    ExpressionTree getExpression();
}
