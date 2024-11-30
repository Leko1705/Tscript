package com.tscript.compiler.source.tree;

import com.tscript.compiler.source.utils.TreeVisitor;

import java.util.List;

/**
 * A tree node for a constructor definition. For example:
 *
 * For example:
 * <pre>
 *  constructor ( <em>parameters</em> ) <em>block</em>
 *
 *  constructor ( <em>parameters</em> ) : super ( <em>arguments</em> ) <em>block</em>
 * </pre>
 *
 * @see ClassTree
 * @since 1.0
 * @author Lennart Köhler
 */
public interface ConstructorTree extends Tree {

    /**
     * Returns all the modifiers for this definition.
     * @return the modifiers
     */
    ModifiersTree getModifiers();

    /**
     * Returns a list of all defined parameters for this functions
     * @return the parameters
     */
    List<? extends ParameterTree> getParameters();

    /**
     * Returns a list of arguments that are passed to the call.
     * @return the call arguments
     */
    List<? extends ArgumentTree> getSuperArguments();

    /**
     * Returns this constructors body.
     * @return the constructors body
     */
    BlockTree getBody();

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
        return visitor.visitConstructor(this, p);
    }
}
