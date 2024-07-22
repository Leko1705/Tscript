package com.tscript.tscriptc.tree;

/**
 * A tree node for a for loop. For example:
 *
 * For example:
 * <pre>
 *  for <em>var-declaration</em> in <em>expression</em> do
 *      <em>statement</em>
 *
 *  for <em>expression</em> do
 *      <em>statement</em>
 * </pre>
 *
 * @since 1.0
 * @author Lennart Köhler
 */
public interface ForLoopTree extends StatementTree {

    /**
     * Returns the defined updated variable for this for loop.
     * @return the updated variable
     */
    VarDefTree getVariable();

    /**
     * Returns the expression over which is iterated.
     * @return the iterable
     */
    ExpressionTree getIterable();

    /**
     * Returns the statement for this loop to be executed in one iteration.
     * @return the loop body
     */
    StatementTree getStatement();

}
