package com.tscript.compiler.source.tree;

import com.tscript.compiler.source.utils.TreeVisitor;

import java.util.List;

/**
 * The root tree for a compiled file.
 *
 * @since 1.0
 * @author Lennart Köhler
 */
public interface RootTree extends Tree {

    /**
     * Returns the name for this module or {@code null}
     * if this is not a module source.
     * @return this module name
     */
    String getModuleName();

    /**
     * Returns all imports for this module. All Trees are either typeof
     * {@link ImportTree} or {@link FromImportTree}.
     * @return all imports
     */
    List<? extends Tree> getImports();

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
