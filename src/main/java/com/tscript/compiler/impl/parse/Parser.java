package com.tscript.compiler.impl.parse;

import com.tscript.compiler.source.tree.ExpressionTree;
import com.tscript.compiler.source.tree.RootTree;
import com.tscript.compiler.source.tree.StatementTree;

/**
 * A Parser converts a sequence of Tokens into its abstract syntax tree.
 * @since 1.0
 * @author Lennart Köhler
 */
public interface Parser {

    /**
     * Parses the complete source
     * @return the root node
     */
    RootTree parseProgram();

    /**
     * Parses a statement
     * @return a statement node
     */
    StatementTree parseStatement();

    /**
     * Parses an expression
     * @return an expression node
     */
    ExpressionTree parseExpression();

}
