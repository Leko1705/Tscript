package com.tscript.compiler.source.tree;

import com.tscript.compiler.source.utils.TreeVisitor;

import java.util.List;

/**
 * A tree node for an array literal. For example:
 *
 * For example:
 * <pre>
 *   []
 *
 *   [ <em>expression</em> ]
 *
 *   [ <em>expression</em> , <em>expression</em> ]
 * </pre>
 *
 * @since 1.0
 * @author Lennart Köhler
 */
public interface ArrayTree extends ExpressionTree {

    /**
     * Returns the contents of this array.
     * @return the contents of this array
     */
    List<? extends ExpressionTree> getContents();

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
        return visitor.visitArray(this, p);
    }
}
