package com.tscript.compiler.source.tree;

import com.tscript.compiler.source.utils.TreeVisitor;

/**
 * A tree node for a continue statement. For example:
 *
 * For example:
 * <pre>
 *  continue ;
 * </pre>
 *
 * @since 1.0
 * @author Lennart Köhler
 */
public interface ContinueTree extends StatementTree {

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
        return visitor.visitContinue(this, p);
    }
}
