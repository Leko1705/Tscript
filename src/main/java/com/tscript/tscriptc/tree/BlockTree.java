package com.tscript.tscriptc.tree;

import java.util.List;

/**
 * A tree node for a block statement. For example:
 *
 * For example:
 * <pre>
 *   { }
 *
 *  { <em>statement</em> }
 * </pre>
 *
 * @since 1.0
 * @author Lennart Köhler
 */
public interface BlockTree extends StatementTree {

    /**
     * Returns the sequence of statements that are executed.
     * @return the statement sequence
     */
    List<? extends StatementTree> getStatements();

}
