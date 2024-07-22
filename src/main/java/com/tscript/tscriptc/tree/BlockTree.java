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
        return visitor.visitBlock(this, p);
    }
}
