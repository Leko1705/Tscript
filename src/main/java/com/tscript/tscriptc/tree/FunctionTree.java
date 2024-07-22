package com.tscript.tscriptc.tree;

import com.tscript.tscriptc.utils.TreeVisitor;

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

    /**
     * {@inheritDoc}
     * @param visitor the visitor to be called
     * @param p a value to be passed to the visitor
     * @return {@inheritDoc}
     * @param <P> {@inheritDoc}
     * @param <R> {@inheritDoc}
     */
    @Override
    default <P, R> R accept(TreeVisitor<P, R> visitor, P p) {
        return visitor.visitFunction(this, p);
    }
}
