package com.tscript.tscriptc.tree;

/**
 * A tree node for a member access expression. For example:
 *
 * For example:
 * <pre>
 *   <em>expression</em> . <em>name</em>
 * </pre>
 *
 * @since 1.0
 * @author Lennart Köhler
 */
public interface MemberAccessTree extends ExpressionTree {

    /**
     * Returns the accessed value.
     * @return the accessed value
     */
    ExpressionTree getExpression();

    /**
     * Returns the member name being accessed
     * @return the member name
     */
    String getMemberName();

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
        return visitor.visitMemberAccess(this, p);
    }
}
