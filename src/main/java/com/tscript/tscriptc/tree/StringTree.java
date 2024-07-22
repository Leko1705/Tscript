package com.tscript.tscriptc.tree;

import com.tscript.tscriptc.utils.TreeVisitor;

/**
 * A tree node for a string literal. For example:
 *
 * For example:
 * <pre>
 *   "hello world"
 * </pre>
 *
 * @since 1.0
 * @author Lennart Köhler
 */
public interface StringTree extends LiteralTree<String> {

    /**
     * {@inheritDoc}
     * In this case a <code>String</code>.
     */
    String get();

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
        return visitor.visitString(this, p);
    }
}
