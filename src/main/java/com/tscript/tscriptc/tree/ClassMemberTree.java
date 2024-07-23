package com.tscript.tscriptc.tree;

/**
 * A tree node used as the base class for the different types class
 * members.
 *
 * @see ClassMemberTree
 * @since 1.0
 * @author Lennart Köhler
 */
public interface ClassMemberTree extends Tree {

    /**
     * Returns all the modifiers for this definition.
     * @return the modifiers
     */
    ModifiersTree getModifiers();

}
