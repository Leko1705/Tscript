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
        return visitor.visitNot(this, p);
    }
}
