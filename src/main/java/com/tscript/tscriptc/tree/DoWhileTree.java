package com.tscript.tscriptc.tree;

import com.tscript.tscriptc.utils.TreeVisitor;

/**
 * A tree node for a do while loop. For example:
 *
 * For example:
 * <pre>
 *  do
 *      <em>statement</em>
 *  while <em>expression</em> ;
 * </pre>
 *
 * @since 1.0
 * @author Lennart Köhler
 */
public interface DoWhileTree extends StatementTree {

    /**
     * The statement being executed at least once for a positive condition.
     * @return the loop body
     */
    StatementTree getStatement();

    /**
     * Returns the condition for this loop
     * @return the condition
     */
    ExpressionTree getCondition();

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
        return visitor.visitDoWhileLoop(this, p);
    }
}
