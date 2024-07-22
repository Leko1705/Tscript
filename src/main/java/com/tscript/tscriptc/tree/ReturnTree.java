package com.tscript.tscriptc.tree;

import com.tscript.tscriptc.utils.TreeVisitor;

/**
 * A tree node for a try catch statement. For example:
 *
 * For example:
 * <pre>
 *  return ;
 *
 *  return <em>expression</em> ;
 * </pre>
 *
 * @since 1.0
 * @author Lennart Köhler
 */
public interface ReturnTree extends StatementTree {

    /**
     * Returns the expression to be returned by this statement.
     * @return the returned expression
     */
    ExpressionTree getExpression();

    /**
     * {@inheritDoc}
     * @param visitor the visitor to be called
     * @param p a value to be passed to the visitor
     * @return {@inheritDoc}
     * @param <P> {@inheritDoc}
     * @param <R>{@inheritDoc}
     */
    @Override
    default <P, R> R accept(TreeVisitor<P, R> visitor, P p) {
        return visitor.visitReturn(this, p);
    }
}
