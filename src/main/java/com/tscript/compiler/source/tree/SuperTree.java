package com.tscript.compiler.source.tree;

import com.tscript.compiler.source.utils.TreeVisitor;

/**
 * A tree node for a super access expression. For example:
 *
 * For example:
 * <pre>
 *   super . <em>name</em>
 * </pre>
 *
 * @since 1.0
 * @author Lennart Köhler
 */
public interface SuperTree extends ExpressionTree {

    /**
     * Returns the name of super accessed field
     * @return the super fields name
     */
    String getName();

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
        return visitor.visitSuper(this, p);
    }
}
