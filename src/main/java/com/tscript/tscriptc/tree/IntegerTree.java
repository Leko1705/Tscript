package com.tscript.tscriptc.tree;

/**
 * A tree node for a integer literal. For example:
 *
 * For example:
 * <pre>
 *   2, 0xFF, 0o07, 0b01011
 * </pre>
 *
 * @since 1.0
 * @author Lennart Köhler
 */
public interface IntegerTree extends LiteralTree<Integer> {

    /**
     * {@inheritDoc}
     * In this case a <code>Integer</code>.
     */
    Integer get();
}
