package com.tscript.tscriptc.parse;

import com.tscript.tscriptc.log.Logger;
import com.tscript.tscriptc.tree.ExpressionTree;
import com.tscript.tscriptc.tree.RootTree;
import com.tscript.tscriptc.tree.StatementTree;

public interface Parser {

    RootTree parseProgram();

    StatementTree parseStatement();

    ExpressionTree parseExpression();

}
