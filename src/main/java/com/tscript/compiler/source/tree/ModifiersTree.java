package com.tscript.compiler.source.tree;

import com.tscript.compiler.source.utils.TreeVisitor;

import java.util.Set;

/**
 * A tree node for Modifiers. For example:
 *
 * <pre>
 *  public
 *
 *  protected
 *
 *  private
 *
 *  static
 *
 *  overridden
 *
 *  abstract
 *
 *  native
 *
 *  const
 * </pre>
 *
 * @since 1.0
 * @author Lennart Köhler
 */
public interface ModifiersTree extends Tree {

    /**
     * Returns a set of all modifier flags.
     * @return the modifiers
     */
    Set<Modifier> getFlags();

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
        return visitor.visitModifiers(this, p);
    }
}
