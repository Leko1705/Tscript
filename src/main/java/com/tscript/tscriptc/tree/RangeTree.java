package com.tscript.tscriptc.tree;

import com.tscript.tscriptc.utils.TreeVisitor;

/**
 * A tree node for a range. For example:
 *
 * For example:
 * <pre>
 *   <em>expression</em> : <em>expression</em>
 * </pre>
 *
 * @since 1.0
 * @author Lennart Köhler
 */
public interface RangeTree extends ExpressionTree {

    /**
     * Returns the value from which this range starts
     * @return the start value
     */
    ExpressionTree getFrom();

    /**
     * Returns the value at which this range ends.
     * @return the end value
     */
    ExpressionTree getTo();

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
        return visitor.visitRange(this, p);
    }
}
