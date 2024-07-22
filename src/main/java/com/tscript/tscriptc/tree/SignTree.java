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

    /**
     * {@inheritDoc}
     * @param visitor the visitor to be called
     * @param p a value to be passed to the visitor
     * @return {@inheritDoc}
     * @param <P> {@inheritDoc}
     * @param <R> {@inheritDoc}
     */
    @Override
    default <P, R> R accept(TreeVisitor<P, R> visitor, P p) {
        return visitor.visitSign(this, p);
    }
}
