package com.tscript.tscriptc.tree;

/**
 * A tree node for an assignment. For example:
 *
 * For example:
 * <pre>
 *   <em>expression</em> = <em>expression</em>
 * </pre>
 *
 * @since 1.0
 * @author Lennart Köhler
 */
public interface AssignTree extends BinaryExpressionTree {

    /**
     * {@inheritDoc}
     */
    ExpressionTree getLeftOperand();

    /**
     * {@inheritDoc}
     */
    ExpressionTree getRightOperand();
}
