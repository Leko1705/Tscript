package com.tscript.compiler.source.tree;

import com.tscript.compiler.source.utils.TreeVisitor;

/**
 * A tree node for a lambda closure. For example:
 *
 * For example:
 * <pre>
 *  <em>name</em> = <em>expression</em>
 * </pre>
 *
 * @see LambdaTree
 * @since 1.0
 * @author Lennart Köhler
 */
public interface ClosureTree extends Tree {

    /**
     * Returns the name of this closure
     * @return the closures name
     */
    String getName();

    /**
     * Returns the value passed for this closure or <code>null</code> if
     * there is none.
     * @return the closures value
     */
    ExpressionTree getInitializer();

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
        return visitor.visitClosure(this, p);
    }
}
