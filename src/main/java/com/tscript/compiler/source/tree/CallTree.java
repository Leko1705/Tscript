package com.tscript.compiler.source.tree;

import com.tscript.compiler.source.utils.TreeVisitor;

import java.util.List;

/**
 * A tree node for a call expression. For example:
 *
 * For example:
 * <pre>
 *   <em>expression</em> ( )
 *
 *   <em>expression</em> ( <em>argument</em> )
 *
 *   <em>expression</em> ( <em>argument</em> , <em>argument</em> )
 * </pre>
 *
 * @since 1.0
 * @author Lennart Köhler
 */
public interface CallTree extends ExpressionTree {

    /**
     * Returns the expression being called.
     * @return the called expression
     */
    ExpressionTree getCalled();

    /**
     * Returns a list of arguments that are passed to the call.
     * @return the call arguments
     */
    List<? extends ArgumentTree> getArguments();

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
        return visitor.visitCall(this, p);
    }
}
