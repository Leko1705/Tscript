package com.tscript.tscriptc.tree;

import java.util.List;

/**
 * A tree node for a function definition. For example:
 *
 * For example:
 * <pre>
 *  function ( <em>parameters</em> ) <em>block</em>
 * </pre>
 *
 * @since 1.0
 * @author Lennart Köhler
 */
public interface FunctionTree extends DefinitionTree, StatementTree {

    /**
     * {@inheritDoc}
     */
    ModifiersTree getModifiers();

    /**
     * Returns this functions name.
     * @return this functions name
     */
    String getName();

    /**
     * Returns a list of all defined parameters for this functions
     * @return the parameters
     */
    List<? extends ParameterTree> getParameters();

    /**
     * Returns this functions body.
     * @return the function body
     */
    BlockTree getBody();

}
