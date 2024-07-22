package com.tscript.tscriptc.tree;

/**
 * A tree node for this expression. For example:
 *
 * For example:
 * <pre>
 *   this
 * </pre>
 *
 * @since 1.0
 * @author Lennart Köhler
 */
public interface ThisTree extends ExpressionTree {

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
        return visitor.visitThis(this, p);
    }
}
