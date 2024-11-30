package com.tscript.compiler.source.tree;

import com.tscript.compiler.source.utils.TreeVisitor;

/**
 * A tree node for variables, names and identifiers.
 *
 * @since 1.0
 * @author Lennart Köhler
 */
public interface VariableTree extends ExpressionTree {

    /**
     * Returns the name of this variable
     * @return the name of this variable
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
        return visitor.visitVariable(this, p);
    }
}
