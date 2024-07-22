package com.tscript.tscriptc.tree;

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

}
