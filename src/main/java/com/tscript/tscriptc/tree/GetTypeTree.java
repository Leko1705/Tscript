package com.tscript.tscriptc.tree;

/**
 * A tree node for a typeof expression. For example:
 *
 * For example:
 * <pre>
 *   typeof <em>expression</em>
 * </pre>
 *
 * @since 1.0
 * @author Lennart Köhler
 */
public interface GetTypeTree extends UnaryExpressionTree {

    /**
     * {@inheritDoc}
     */
    ExpressionTree getOperand();
}
