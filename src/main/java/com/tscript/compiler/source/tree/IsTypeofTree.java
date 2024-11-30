package com.tscript.compiler.source.tree;

import com.tscript.compiler.source.utils.TreeVisitor;

/**
 * A tree node for a type checking operation. For example:
 *
 * For example:
 * <pre>
 *   <em>expression</em> <em>typeof</em> <em>expression</em>
 * </pre>
 *
 * @since 2.0
 * @author Lennart Köhler
 */
public interface IsTypeofTree extends ExpressionTree {

    /**
     * The value to check for the type
     * @return the checked value
     */
    ExpressionTree getChecked();

    /**
     * The type to check the value against
     * @return the checked type
     */
    ExpressionTree getType();

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
        return visitor.visitIsTypeofTree(this, p);
    }
}
