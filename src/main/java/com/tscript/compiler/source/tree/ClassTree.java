package com.tscript.compiler.source.tree;

import com.tscript.compiler.source.utils.TreeVisitor;

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
     * Returns a list of all members defined in this class.
     * @return the members of this class
     */
    List<? extends Tree> getMembers();

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
        return visitor.visitClass(this, p);
    }
}
