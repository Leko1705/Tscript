package com.tscript.tscriptc.tree;

/**
 * A tree node for a float literal. For example:
 *
 * For example:
 * <pre>
 *   0.5
 * </pre>
 *
 * @since 1.0
 * @author Lennart Köhler
 */
public interface FloatTree extends LiteralTree<Double> {

    /**
     * {@inheritDoc}
     * In this case a <code>Double</code>.
     */
    Double get();
}
