package com.tscript.tscriptc.tree;

import com.tscript.tscriptc.utils.TreeVisitor;

/**
 * A tree node for a container access expression. For example:
 *
 * For example:
 * <pre>
 *   <em>expression</em> [ <em>expression</em> ]
 * </pre>
 *
 * @since 1.0
 * @author Lennart Köhler
 */
public interface ContainerAccessTree extends ExpressionTree {

    /**
     * Returns the value accessed by the container access operator
     * @return the accessed container
     */
    ExpressionTree getContainer();

    /**
     * Returns the key for this container access
     * @return the key
     */
    ExpressionTree getKey();

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
        return visitor.visitContainerAccess(this, p);
    }
}
