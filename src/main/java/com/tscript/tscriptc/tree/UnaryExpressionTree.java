package com.tscript.tscriptc.tree;

/**
 * A tree node used as the base class for the different types of
 * unary expression.
 * @since 1.0
 * @author Lennart Köhler
 */
public interface UnaryExpressionTree extends ExpressionTree {

    /**
     * Returns the operand on which this unary operation is performed.
     * @return the operand
     */
    ExpressionTree getOperand();

}
