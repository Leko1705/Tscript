package com.tscript.compiler.source.tree;

import com.tscript.compiler.source.utils.TreeVisitor;

import java.util.List;

/**
 * A tree node for namespaces. For example:
 *
 * For example:
 * <pre>
 *   namespace <em>name</em> <em>block</em>
 * </pre>
 *
 * @since 1.0
 * @author Lennart Köhler
 */
public interface NamespaceTree extends DefinitionTree {

    /**
     * {@inheritDoc}
     */
    ModifiersTree getModifiers();

    /**
     * {@inheritDoc}
     */
    String getName();

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
        return visitor.visitNamespace(this, p);
    }
}
