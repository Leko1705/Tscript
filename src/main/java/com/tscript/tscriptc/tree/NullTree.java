package com.tscript.tscriptc.tree;

/**
 * A tree node for a null literal. For example:
 *
 * For example:
 * <pre>
 *   null
 * </pre>
 *
 * @since 1.0
 * @author Lennart Köhler
 */
public interface NullTree extends LiteralTree<Void> {

    /**
     * {@inheritDoc}
     * This method returns <code>null</code>
     */
    Void get();
}
