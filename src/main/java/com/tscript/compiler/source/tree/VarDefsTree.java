package com.tscript.compiler.source.tree;

import com.tscript.compiler.source.utils.TreeVisitor;

import java.util.List;

/**
 * A tree node for a multiple variable definition statement. For example:
 *
 * For example:
 * <pre>
 *  var <em>name</em> = <em>expression</em> , <em>name</em> ;
 *
 *  const <em>name</em> = <em>expression</em> , <em>name</em> = <em>expression</em> ;
 *
 * </pre>
 *
 * @see VarDefTree
 * @since 1.0
 * @author Lennart Köhler
 */
public interface VarDefsTree extends StatementTree {

    /**
     * Returns all the modifiers for these variable definitions.
     * @return the modifiers
     */
    ModifiersTree getModifiers();

    /**
     * Returns a list of all variables defined by these modifiers.
     * @return the defined variables
     * @see #getModifiers()
     */
    List<? extends VarDefTree> getDefinitions();

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
        return visitor.visitVarDefs(this, p);
    }
}
