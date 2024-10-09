package com.tscript.compiler.source.tree;

import com.tscript.compiler.source.utils.TreeVisitor;

import java.util.List;

/**
 * A tree node for an import statement. For example:
 *
 * For example:
 * <pre>
 *  import <em>name</em> ;
 *
 *  import <em>name</em> . <em>name</em> ;
 * </pre>
 *
 * @since 1.0
 * @author Lennart Köhler
 */
public interface ImportTree extends Tree {

    /**
     * Returns the access chain for this import.
     * @return the access chain
     */
    List<String> getAccessChain();

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
        return visitor.visitImport(this, p);
    }
}
