package com.tscript.tscriptc.tree;

import com.tscript.tscriptc.utils.TreeVisitor;

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
        return visitor.visitGetType(this, p);
    }
}
