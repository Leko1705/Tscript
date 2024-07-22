package com.tscript.tscriptc.tree;

import java.util.List;

/**
 * A tree node for a class definition. For example:
 *
 * For example:
 * <pre>
 *  class <em>name</em> <em>class-body</em>
 *
 *  class <em>name</em> : <em>name</em> <em>class-body</em>
 * </pre>
 *
 * @since 1.0
 * @author Lennart Köhler
 */
public interface ClassTree extends DefinitionTree, StatementTree {

    /**
     * {@inheritDoc}
     */
    ModifiersTree getModifiers();

    /**
     * Returns the name of this class
     * @return the class name
     */
    String getName();

    /**
     * Returns the name of the inherited class or <code>null</code> if this
     * class does not inherit any other class.
     * @return the inherited class name
     */
    String getSuperName();

    /**
     * Returns the Constructor for this class or <code>null</code> if
     * the implicit default constructor is used.
     * @return the constructor
     */
    ConstructorTree getConstructor();

    /**
     * Returns a list of all members defined in this class.
     * @return the members of this class
     */
    List<? extends DefinitionTree> getMembers();

}
