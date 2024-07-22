package com.tscript.tscriptc.tree;

/**
 * A tree node for a throw statement. For example:
 *
 * For example:
 * <pre>
 *  throw <em>expression</em> ;
 * </pre>
 *
 * @since 1.0
 * @author Lennart Köhler
 */
public interface ThrowTree extends StatementTree {

    /**
     * Returns the expression to be thrown.
     * @return the thrown expression
     */
    ExpressionTree getThrown();

}
