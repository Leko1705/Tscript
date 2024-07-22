package com.tscript.tscriptc.tree;

/**
 * A tree node for a variable definition statement. For example:
 *
 * For example:
 * <pre>
 *  var <em>name</em> ;
 *
 *  var <em>name</em> = <em>expression</em> ;
 *
 *  const <em>name</em> = <em>expression</em> ;
 * </pre>
 *
 * @see VarDefsTree
 * @since 1.0
 * @author Lennart Köhler
 */
public interface VarDefTree extends Tree {

    /**
     * Returns the name of this variable.
     * @return the variable nam
     */
    String getName();

    /**
     * Returns the value assigned to the variable or null if this
     * variable is not initialized.
     * @return the initial value for this variable
     */
    ExpressionTree getInitializer();

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
        return visitor.visitVarDef(this, p);
    }
}
