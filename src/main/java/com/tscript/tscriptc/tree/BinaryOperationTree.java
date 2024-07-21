package com.tscript.tscriptc.tree;

public interface BinaryOperationTree extends BinaryExpressionTree {

    ExpressionTree getLeftOperand();

    ExpressionTree getRightOperand();

    Operation getOperationType();
}
