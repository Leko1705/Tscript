package com.tscript.tscriptc.tree;

@InheritOnly
public interface LiteralTree<T> extends ExpressionTree {

    T get();

}
