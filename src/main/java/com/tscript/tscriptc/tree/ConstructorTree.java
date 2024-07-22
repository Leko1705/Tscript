package com.tscript.tscriptc.tree;

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

    ModifiersTree getModifiers();

    List<? extends ParameterTree> getParameters();

    List<? extends ExpressionTree> getSuperArguments();

    BlockTree getBody();

}
