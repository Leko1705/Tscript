package com.tscript.tscriptc.tree;

/**
 * A tree node for a not expression. For example:
 *
 * For example:
 * <pre>
 *   not <em>expression</em>
 * </pre>
 *
 * @since 1.0
 * @author Lennart Köhler
 */
public interface NotTree extends UnaryExpressionTree {

    /**
     * {@inheritDoc}
     */
    ExpressionTree getOperand();

}
