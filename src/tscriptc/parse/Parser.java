package tscriptc.parse;

import tscriptc.log.Logger;
import tscriptc.tree.ExpressionTree;
import tscriptc.tree.RootTree;
import tscriptc.tree.StatementTree;

public interface Parser {

    RootTree parseProgram();

    StatementTree parseStatement();

    ExpressionTree parseExpression();

}
