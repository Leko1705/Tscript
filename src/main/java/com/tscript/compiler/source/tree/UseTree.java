package com.tscript.compiler.source.tree;

import com.tscript.compiler.source.utils.TreeVisitor;

/**
 * A tree node for a use statement. For example:
 *
 * For example:
 * <pre>
 *  use <em>variable</em> ;
 * </pre>
 *
 * @since 1.0
 * @author Lennart Köhler
 */
public interface UseTree extends StatementTree {

    /**
     * The used expression
     * @return the variable to be used
     */
    ExpressionTree getUsed();

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
        return visitor.visitUse(this, p);
    }
}
