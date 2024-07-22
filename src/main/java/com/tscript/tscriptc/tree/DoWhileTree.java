package com.tscript.tscriptc.tree;

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

}
