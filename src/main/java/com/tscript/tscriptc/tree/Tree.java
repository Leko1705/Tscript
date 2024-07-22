package com.tscript.tscriptc.tree;

import com.tscript.tscriptc.utils.Location;

/**
 * Common Interface for all Nodes in the Abstract-Syntax-Tree.
 * @since 1.0
 * @author Lennart Köhler
 */
public interface Tree {

    /**
     * Returns the Location of this node in the source code.
     * @return the Location of this node in the source code.
     */
    Location getLocation();

    /**
     * Accept method used to implement the visitor pattern.  The
     * visitor pattern is used to implement operations on trees.
     *
     * @param visitor the visitor to be called
     * @param p a value to be passed to the visitor
     * @return the result returned from calling the visitor
     * @param <P> the type of additional data
     * @param <R> the result type of this operation
     */
    <P, R> R accept(TreeVisitor<P, R> visitor, P p);

    /**
     * A lazy implementation of the {@link #accept(TreeVisitor, Object)} method for
     * passing no arguments. The default implementation calls {@link #accept(TreeVisitor, Object)}
     * with {@code null} as additional argument.
     *
     * @param visitor the visitor to be called
     * @return the result returned from calling the visitor
     * @param <P> the type of default additional data
     * @param <R> the result of this operation
     */
    default <P, R> R accept(TreeVisitor<P, R> visitor){
        return accept(visitor, null);
    }

}
