package com.tscript.tscriptc.tree;

public interface ContainerAccessTree extends ExpressionTree {

    ExpressionTree getContainer();

    ExpressionTree getKey();
}
