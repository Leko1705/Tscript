package com.tscript.compiler.source.tree;

import com.tscript.compiler.source.utils.TreeVisitor;

import java.util.List;

/**
 * A tree node for a switch statement. For example:
 *
 * For example:
 * <pre>
 *  switch <em>expression</em> then { <em>case-list</em> }
 *  switch <em>expression</em> then { <em>case-list</em> <em>default-case</em> }
 * </pre>
 *
 * @since 2.0
 * @author Lennart Köhler
 */
public interface SwitchTree extends StatementTree {

    /**
     * The evaluated expression for this switch statement
     * @return the evaluated expression
     */
    ExpressionTree getExpression();

    /**
     * The cases for this switch to check the expression against
     * @return this switch's cases
     */
    List<? extends CaseTree> getCases();

    /**
     * Returns the default case for this Switch, or {@code null}
     * if none is provided.
     * @return the default case, if existent
     */
    StatementTree getDefaultCase();

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
        return visitor.visitSwitch(this, p);
    }
}
