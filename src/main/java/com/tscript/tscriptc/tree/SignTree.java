package com.tscript.tscriptc.tree;

/**
 * A tree node for a signed expression. For example:
 *
 * For example:
 * <pre>
 *   - <em>expression</em>
 *
 *   + <em>expression</em>
 * </pre>
 *
 * @since 1.0
 * @author Lennart Köhler
 */
public interface SignTree extends UnaryExpressionTree {

    /**
     * Returns <em>true</em> if the expression is negated, for example
     * <em>-x</em>, <em>false</em> otherwise.
     * @return
     */
    boolean isNegation();

    /**
     * {@inheritDoc}
     */
    ExpressionTree getOperand();
}
