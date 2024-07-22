package com.tscript.tscriptc.tree;

import com.tscript.tscriptc.utils.TreeVisitor;

/**
 * A tree node for a integer literal. For example:
 *
 * For example:
 * <pre>
 *   2, 0xFF, 0o07, 0b01011
 * </pre>
 *
 * @since 1.0
 * @author Lennart Köhler
 */
public interface IntegerTree extends LiteralTree<Integer> {

    /**
     * {@inheritDoc}
     * In this case a <code>Integer</code>.
     */
    Integer get();

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
        return visitor.visitInteger(this, p);
    }
}
