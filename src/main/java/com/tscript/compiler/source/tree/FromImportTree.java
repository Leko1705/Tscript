package com.tscript.compiler.source.tree;

import com.tscript.compiler.source.utils.TreeVisitor;

import java.util.List;

/**
 * A tree node for a from import statement. For example:
 *
 * For example:
 * <pre>
 *  from <em>name</em> import <em>name</em> ;
 *
 *  from <em>name</em> . <em>name</em> import <em>name</em> . <em>name</em> ;
 *
 *  from <em>name</em> import * ;
 * </pre>
 *
 * @since 2.0
 * @author Lennart Köhler
 */
public interface FromImportTree extends Tree {

    /**
     * Returns the access chain for the accessed module.
     * @return the access chain for the module
     */
    List<String> getFromAccessChain();

    /**
     * Returns the access chain for the definition.
     * @return the access chain for the definition
     */
    List<String> getImportAccessChain();

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
        return visitor.visitFromImport(this, p);
    }
}
