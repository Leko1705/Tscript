package com.tscript.compiler.source.tree;

import com.tscript.compiler.source.utils.TreeVisitor;

import java.util.List;

/**
 * A tree node for a dictionary literal. For example:
 *
 * For example:
 * <pre>
 *   { }
 *
 *   { <em>expression</em> : <em>expression</em> }
 *
 *   { <em>expression</em> : <em>expression</em> , <em>expression</em> : <em>expression</em> }
 * </pre>
 *
 * @since 1.0
 * @author Lennart Köhler
 */
public interface DictionaryTree extends ExpressionTree {

    /**
     * The List of keys in this dictionary. The i-th key refers to the
     * i-th value in {@link #getValues()}.
     * @return the key values
     */
    List<? extends ExpressionTree> getKeys();

    /**
     * The List of values in this dictionary. The i-th value referred by the
     * i-th key in {@link #getKeys()}.
     * @return the values
     */
    List<? extends ExpressionTree> getValues();

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
        return visitor.visitDictionary(this, p);
    }
}
