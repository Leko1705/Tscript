package com.tscript.tscriptc.tree;

import com.tscript.tscriptc.utils.TreeVisitor;

/**
 * A tree node for a float literal. For example:
 *
 * For example:
 * <pre>
 *   0.5
 * </pre>
 *
 * @since 1.0
 * @author Lennart Köhler
 */
public interface FloatTree extends LiteralTree<Double> {

    /**
     * {@inheritDoc}
     * In this case a <code>Double</code>.
     */
    Double get();

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
        return visitor.visitFloat(this, p);
    }
}
