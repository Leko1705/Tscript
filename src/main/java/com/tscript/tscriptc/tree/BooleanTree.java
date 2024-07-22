package com.tscript.tscriptc.tree;

/**
 * A tree node for a boolean literal. For example:
 *
 * For example:
 * <pre>
 *   true, false
 * </pre>
 *
 * @since 1.0
 * @author Lennart Köhler
 */
public interface BooleanTree extends LiteralTree<Boolean> {

    /**
     * {@inheritDoc}
     * In this case a <code>Boolean</code>.
     */
    Boolean get();
}
