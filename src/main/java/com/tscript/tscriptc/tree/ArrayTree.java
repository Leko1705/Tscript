package com.tscript.tscriptc.tree;

import java.util.List;

/**
 * A tree node for an array literal. For example:
 *
 * For example:
 * <pre>
 *   []
 *
 *   [ <em>expression</em> ]
 *
 *   [ <em>expression</em> , <em>expression</em> ]
 * </pre>
 *
 * @since 1.0
 * @author Lennart Köhler
 */
public interface ArrayTree extends ExpressionTree {

    /**
     * Returns the contents of this array.
     * @return the contents of this array
     */
    List<? extends ExpressionTree> getContents();

}
