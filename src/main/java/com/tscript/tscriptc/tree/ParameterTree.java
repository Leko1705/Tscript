package com.tscript.tscriptc.tree;

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

}
