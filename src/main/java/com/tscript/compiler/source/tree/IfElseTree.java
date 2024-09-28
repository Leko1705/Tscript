package com.tscript.compiler.source.tree;

import com.tscript.compiler.source.utils.TreeVisitor;

/**
 * A tree node for an if else statement. For example:
 *
 * For example:
 * <pre>
 *  if <em>expression</em> then
 *      <em>statement</em>
 *
 *  if <em>expression</em> then
 *      <em>statement</em>
 *  else
 *      <em>statement</em>
 * </pre>
 *
 * @since 1.0
 * @author Lennart Köhler
 */
public interface IfElseTree extends StatementTree {

    /**
     * The condition for this if statement
     * @return the condition
     */
    ExpressionTree getCondition();

    /**
     * The statement being executed if the condition is <code>true</code>.
     * @return the on-true statement
     */
    StatementTree getThenStatement();

    /**
     * The statement being executed if the condition is <code>false</code>.
     * @return the on-false statement
     */
    StatementTree getElseStatement();

    /**
     * {@inheritDoc}
     * @param visitor the visitor to be called
     * @param p a value to be passed to the visitor
     * @return {@inheritDoc}
     * @param <P> {@inheritDoc}
     * @param <R {@inheritDoc}
     */
    @Override
    default <P, R> R accept(TreeVisitor<P, R> visitor, P p) {
        return visitor.visitIfElse(this, p);
    }
}
