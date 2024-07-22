package com.tscript.tscriptc.tree;

/**
 * A tree node for a container access expression. For example:
 *
 * For example:
 * <pre>
 *   <em>expression</em> [ <em>expression</em> ]
 * </pre>
 *
 * @since 1.0
 * @author Lennart Köhler
 */
public interface ContainerAccessTree extends ExpressionTree {

    /**
     * Returns the value accessed by the container access operator
     * @return the accessed container
     */
    ExpressionTree getContainer();

    /**
     * Returns the key for this container access
     * @return the key
     */
    ExpressionTree getKey();
}
