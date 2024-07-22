package com.tscript.tscriptc.tree;

/**
 * A tree node for an assignment. For example:
 *
 * For example:
 * <pre>
 *   <em>expression</em> = <em>expression</em>
 * </pre>
 *
 * @since 1.0
 * @author Lennart Köhler
 */
public interface AssignTree extends BinaryExpressionTree {

    /**
     * {@inheritDoc}
     */
    ExpressionTree getLeftOperand();

    /**
     * {@inheritDoc}
     */
    ExpressionTree getRightOperand();

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
        return visitor.visitAssign(this, p);
    }
}
