package com.tscript.tscriptc.tree;

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

}
