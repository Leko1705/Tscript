package com.tscript.tscriptc.tree;

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
     * Returns a set of all modifiers.
     * @return the modifiers
     */
    Set<Modifier> getModifiers();

}
