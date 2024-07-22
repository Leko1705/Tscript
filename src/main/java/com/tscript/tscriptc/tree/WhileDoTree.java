package com.tscript.tscriptc.tree;

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

}
