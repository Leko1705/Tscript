package com.tscript.tscriptc.tree;

public interface ClosureTree extends Tree {

    String getName();

    ExpressionTree getInitializer();

}
