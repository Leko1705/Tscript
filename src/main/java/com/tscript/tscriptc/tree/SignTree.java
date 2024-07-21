package com.tscript.tscriptc.tree;

public interface SignTree extends UnaryExpressionTree {

    boolean isNegation();

    ExpressionTree getOperand();
}
