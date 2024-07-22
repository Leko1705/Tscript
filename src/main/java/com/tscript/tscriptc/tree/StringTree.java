package com.tscript.tscriptc.tree;

/**
 * A tree node for a string literal. For example:
 *
 * For example:
 * <pre>
 *   "hello world"
 * </pre>
 *
 * @since 1.0
 * @author Lennart Köhler
 */
public interface StringTree extends LiteralTree<String> {

    /**
     * {@inheritDoc}
     * In this case a <code>String</code>.
     */
    String get();
}
