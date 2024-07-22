package com.tscript.tscriptc.tree;

/**
 * A tree node used as the base class for the different types of
 * definitions.
 * @since 1.0
 * @author Lennart Köhler
 */
public interface DefinitionTree extends Tree {

    /**
     * Returns all the modifiers for this definition.
     * @return the modifiers
     */
    ModifiersTree getModifiers();

    /**
     * Returns the name of this defined element
     * @return the name of this defined element
     */
    String getName();
}
