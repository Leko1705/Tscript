package com.tscript.tscriptc.tree;

public interface BinaryExpressionTree extends ExpressionTree {

    ExpressionTree getLeftOperand();

    ExpressionTree getRightOperand();

}
