package com.tscript.tscriptc.tree;

import com.tscript.tscriptc.utils.TreeVisitor;

/**
 * A tree node for parameters. For example:
 *
 * For example:
 * <pre>
 *   <em>name</em>
 *
 *   <em>name</em> = <em>expression</em>
 * </pre>
 *
 * @since 1.0
 * @author Lennart Köhler
 */
public interface ParameterTree extends DefinitionTree {

    /**
     * {@inheritDoc}
     */
    ModifiersTree getModifiers();

    /**
     * {@inheritDoc}
     */
    String getName();

    /**
     * Returns the default value for this parameter or <code>null</code> if
     * there is none.
     * @return the default value
     */
    ExpressionTree getDefaultValue();

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
        return visitor.visitParameter(this, p);
    }
}
