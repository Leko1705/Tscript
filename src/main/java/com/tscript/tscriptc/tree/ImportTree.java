package com.tscript.tscriptc.tree;

import java.util.List;

/**
 * A tree node for an import statement. For example:
 *
 * For example:
 * <pre>
 *  import <em>name</em> ;
 *
 *  import <em>name</em> . <em>name</em> ;
 * </pre>
 *
 * @since 1.0
 * @author Lennart Köhler
 */
public interface ImportTree extends StatementTree {

    /**
     * Returns the access chain for this import.
     * @return the access chain
     */
    List<String> getAccessChain();

}
