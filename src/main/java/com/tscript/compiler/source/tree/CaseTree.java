package com.tscript.compiler.source.tree;

import com.tscript.compiler.source.utils.TreeVisitor;

/**
 * A tree node for a switch's case statement. For example:
 *
 * For example:
 * <pre>
 *  case <em>expression</em> do <em>statement</em>
 * </pre>
 *
 * @since 2.0
 * @author Lennart Köhler
 */
public interface CaseTree extends Tree {

    /**
     * The expression of to check the switched value against
     * @return the checked expression
     */
    ExpressionTree getExpression();

    /**
     * The statement to execute on match.
     * @return the executed statement
     */
    StatementTree getStatement();

    /**
     * Returns true if this case allows to terminate the
     * switch execution, including the default case, else false.
     * @return true if this case can break the switch
     */
    boolean allowBreak();

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
        return visitor.visitCase(this, p);
    }
}
