package com.tscript.compiler.source.tree;

import com.tscript.compiler.source.utils.TreeVisitor;

/**
 * A tree node for a while do loop. For example:
 *
 * For example:
 * <pre>
 *  while <em>expression</em> do
 *      <em>statement</em> ;
 * </pre>
 *
 * @since 1.0
 * @author Lennart Köhler
 */
public interface WhileDoTree extends StatementTree {

    /**
     * Returns the condition for this loop
     * @return the condition
     */
    ExpressionTree getCondition();

    /**
     * The statement being executed for a positive condition.
     * @return the loop body
     */
    StatementTree getStatement();

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
        return visitor.visitWhileDoLoop(this, p);
    }
}
