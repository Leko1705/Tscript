package com.tscript.tscriptc.tree;

import java.util.List;

/**
 * A tree node for a call expression. For example:
 *
 * For example:
 * <pre>
 *   <em>expression</em> ( )
 *
 *   <em>expression</em> ( <em>argument</em> )
 *
 *   <em>expression</em> ( <em>argument</em> , <em>argument</em> )
 * </pre>
 *
 * @since 1.0
 * @author Lennart Köhler
 */
public interface CallTree extends ExpressionTree {

    /**
     * Returns the expression being called.
     * @return the called expression
     */
    ExpressionTree getCalled();

    /**
     * Returns a list of arguments that are passed to the call.
     * @return the call arguments
     */
    List<? extends ArgumentTree> getArguments();

}
