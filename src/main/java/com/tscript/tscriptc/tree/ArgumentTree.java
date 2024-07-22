package com.tscript.tscriptc.tree;

/**
 * A tree node for a call argument. For example:
 *
 * For example:
 * <pre>
 *  <em>expression</em>
 *
 *  <em>name</em> = <em>expression</em>
 * </pre>
 *
 * @see CallTree
 * @since 1.0
 * @author Lennart Köhler
 */
public interface ArgumentTree extends Tree {

    /**
     * Returns the name of the parameter this argument refers to.
     * @return the referred parameter name
     */
    String getName();

    /**
     * Returns the Expression that is passed for this argument.
     * @return the passed value
     */
    ExpressionTree getExpression();
}
