package com.tscript.tscriptc.tree;

/**
 * A tree node for a null literal. For example:
 *
 * For example:
 * <pre>
 *   null
 * </pre>
 *
 * @since 1.0
 * @author Lennart Köhler
 */
public interface NullTree extends LiteralTree<Void> {

    /**
     * {@inheritDoc}
     * This method returns <code>null</code>
     */
    Void get();

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
        return visitor.visitNull(this, p);
    }
}
