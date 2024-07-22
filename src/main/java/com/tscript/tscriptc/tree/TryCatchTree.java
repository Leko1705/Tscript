package com.tscript.tscriptc.tree;

/**
 * A tree node for a try catch statement. For example:
 *
 * For example:
 * <pre>
 *  try
 *      <em>statement</em>
 *  catch <em>var-declaration</em> do
 *      <em>statement</em>
 * </pre>
 *
 * @since 1.0
 * @author Lennart Köhler
 */
public interface TryCatchTree extends StatementTree {

    /**
     * Returns the statement to be executed in the try block
     * @return the try statement
     */
    StatementTree getTryStatement();

    /**
     * Returns the variable definition in which the caught exception is stored
     * @return the caught exception variable
     */
    VarDefTree getExceptionVariable();

    /**
     * Returns the statement that is executed if an error cures in the try statement.
     * @return the catch statement
     */
    StatementTree getCatchStatement();

}
