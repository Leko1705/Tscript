package com.tscript.tscriptc.tree;

import com.tscript.tscriptc.utils.TreeVisitor;

/**
 * A tree node for a throw statement. For example:
 *
 * For example:
 * <pre>
 *  throw <em>expression</em> ;
 * </pre>
 *
 * @since 1.0
 * @author Lennart Köhler
 */
public interface ThrowTree extends StatementTree {

    /**
     * Returns the expression to be thrown.
     * @return the thrown expression
     */
    ExpressionTree getThrown();

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
        return visitor.visitThrow(this, p);
    }
}
