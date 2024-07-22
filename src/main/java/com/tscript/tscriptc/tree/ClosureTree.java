package com.tscript.tscriptc.tree;

/**
 * A tree node for a lambda closure. For example:
 *
 * For example:
 * <pre>
 *  <em>name</em> = <em>expression</em>
 * </pre>
 *
 * @see LambdaTree
 * @since 1.0
 * @author Lennart Köhler
 */
public interface ClosureTree extends Tree {

    /**
     * Returns the name of this closure
     * @return the closures name
     */
    String getName();

    /**
     * Returns the value passed for this closure.
     * @return the closures value
     */
    ExpressionTree getInitializer();

}
