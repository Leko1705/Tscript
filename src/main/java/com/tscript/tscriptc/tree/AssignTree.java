package com.tscript.tscriptc.tree;

public interface AssignTree extends BinaryExpressionTree {

    ExpressionTree getLeftOperand();

    ExpressionTree getRightOperand();
}
