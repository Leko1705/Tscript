package com.tscript.compiler.source.tree;

/**
 * A tree node used as the base class for the different types of
 * binary expressions.
 * @since 1.0
 * @author Lennart Köhler
 */
public interface BinaryExpressionTree extends ExpressionTree {

    /**
     * Returns the left operand of this expression.
     * @return the left operand of this expression
     */
    ExpressionTree getLeftOperand();

    /**
     * Returns the right operand of this expression.
     * @return the right operand of this expression
     */
    ExpressionTree getRightOperand();

}
