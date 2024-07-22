package com.tscript.tscriptc.tree;

/**
 * A tree node used as the base class for the different types of
 * binary operations. For example:
 *
 * <pre>
 *     <em>expression</em> + <em>expression</em>
 *
 *     <em>expression</em> - <em>expression</em>
 *
 *     <em>expression</em> * <em>expression</em>
 *
 *     <em>expression</em> / <em>expression</em>
 *
 *     <em>expression</em> // <em>expression</em>
 *
 *     <em>expression</em> ^ <em>expression</em>
 *
 *     <em>expression</em> % <em>expression</em>
 *
 *     <em>expression</em> and <em>expression</em>
 *
 *     <em>expression</em> or <em>expression</em>
 *
 *     <em>expression</em> xor <em>expression</em>
 *
 *     <em>expression</em> << <em>expression</em>
 *
 *     <em>expression</em> >> <em>expression</em>
 *
 *     <em>expression</em> >>> <em>expression</em>
 *
 *     <em>expression</em> < <em>expression</em>
 *
 *     <em>expression</em> > <em>expression</em>
 *
 *     <em>expression</em> <= <em>expression</em>
 *
 *     <em>expression</em> >= <em>expression</em>
 *
 *     <em>expression</em> == <em>expression</em>
 *
 *     <em>expression</em> != <em>expression</em>
 * </pre>
 *
 * The type of operation is determined by {@link #getOperationType()}.
 *
 * @since 1.0
 * @author Lennart Köhler
 */
public interface BinaryOperationTree extends BinaryExpressionTree {

    /**
     * {@inheritDoc}
     */
    ExpressionTree getLeftOperand();

    /**
     * {@inheritDoc}
     */
    ExpressionTree getRightOperand();

    /**
     * Returns the Operation type for this node that is performed.
     * @return the operation type
     */
    Operation getOperationType();

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
        return visitor.visitBinaryOperation(this, p);
    }
}
