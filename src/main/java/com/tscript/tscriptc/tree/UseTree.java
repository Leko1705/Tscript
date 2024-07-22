package com.tscript.tscriptc.tree;

/**
 * A tree node for a use statement. For example:
 *
 * For example:
 * <pre>
 *  use <em>variable</em> ;
 * </pre>
 *
 * @since 1.0
 * @author Lennart Köhler
 */
public interface UseTree extends StatementTree {

    /**
     * The variable to be used
     * @return the variable to be used
     */
    VariableTree getVariable();

}
