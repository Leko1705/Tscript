package com.tscript.tscriptc.tree;

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

}
