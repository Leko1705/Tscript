package com.tscript.tscriptc.tree;

import com.tscript.tscriptc.utils.TreeVisitor;

import java.util.List;

/**
 * The root tree for a compiled file.
 *
 * @since 1.0
 * @author Lennart Köhler
 */
public interface RootTree extends Tree {

    /**
     * Returns all definitions for this Namespace.
     * @return the definitions
     */
    List<? extends DefinitionTree> getDefinitions();

    /**
     * Returns all statements being executed inside this namespace.
     * @return all statements
     */
    List<? extends StatementTree> getStatements();

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
        return visitor.visitRoot(this, p);
    }
}
