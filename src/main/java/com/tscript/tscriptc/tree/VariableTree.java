package com.tscript.tscriptc.tree;

/**
 * A tree node for variables, names and identifiers.
 *
 * @since 1.0
 * @author Lennart Köhler
 */
public interface VariableTree extends ExpressionTree {

    /**
     * Returns the name of this variable
     * @return the name of this variable
     */
    String getName();

}
