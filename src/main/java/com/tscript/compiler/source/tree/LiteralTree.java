package com.tscript.compiler.source.tree;

/**
 * A tree node used as the base class for the different types of
 * literals.
 * @since 1.0
 * @author Lennart Köhler
 * @param <T> The equivalent java type for this literal
 */
public interface LiteralTree<T> extends ExpressionTree {

    /**
     * Returns the associated java value for this literal.
     * @return the associated java value
     */
    T get();

}
