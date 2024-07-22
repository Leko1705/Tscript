package com.tscript.tscriptc.tree;

import com.tscript.tscriptc.utils.TreeVisitor;

/**
 * A tree node for an expression statement. For example:
 *
 * For example:
 * <pre>
 *  <em>expression</em> ;
 * </pre>
 *
 * @since 1.0
 * @author Lennart Köhler
 */
public interface ExpressionStatementTree extends StatementTree {

    /**
     * Returns the expression to be performed as a statement.
     * @return the expression
     */
    ExpressionTree getExpression();

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
        return visitor.visitExpressionStatement(this, p);
    }
}
