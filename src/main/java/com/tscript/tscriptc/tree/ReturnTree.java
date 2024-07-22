package com.tscript.tscriptc.tree;

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

}
