package com.tscript.tscriptc.parse;

import com.tscript.tscriptc.tree.ExpressionTree;
import com.tscript.tscriptc.tree.RootTree;
import com.tscript.tscriptc.tree.StatementTree;

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
