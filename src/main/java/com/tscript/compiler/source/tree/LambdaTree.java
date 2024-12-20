package com.tscript.compiler.source.tree;

import com.tscript.compiler.source.utils.TreeVisitor;

import java.util.List;

/**
 * A tree node for a lambda function expression. For example:
 *
 * For example:
 * <pre>
 *  function ( <em>parameters</em> ) <em>block</em>
 *
 *  function [ <em>closures</em> ] ( <em>parameters</em> ) <em>block</em>
 * </pre>
 *
 * @since 1.0
 * @author Lennart Köhler
 */
public interface LambdaTree extends ExpressionTree {

    /**
     * Returns a list of all defined closures. Returns an empty list
     * if none is defined
     * @return the closures
     */
    List<? extends ClosureTree> getClosures();

    /**
     * Returns a list of all defined parameters for this functions
     * @return the parameters
     */
    List<? extends ParameterTree> getParameters();

    /**
     * Returns this functions body.
     * @return the function body
     */
    BlockTree getBody();

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
        return visitor.visitLambda(this, p);
    }
}
