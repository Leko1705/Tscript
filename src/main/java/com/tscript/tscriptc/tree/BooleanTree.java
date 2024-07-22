package com.tscript.tscriptc.tree;

import com.tscript.tscriptc.utils.TreeVisitor;

/**
 * A tree node for a boolean literal. For example:
 *
 * For example:
 * <pre>
 *   true, false
 * </pre>
 *
 * @since 1.0
 * @author Lennart Köhler
 */
public interface BooleanTree extends LiteralTree<Boolean> {

    /**
     * {@inheritDoc}
     * In this case a <code>Boolean</code>.
     */
    Boolean get();

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
        return visitor.visitBoolean(this, p);
    }
}
