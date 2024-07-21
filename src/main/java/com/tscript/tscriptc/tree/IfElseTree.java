package com.tscript.tscriptc.tree;

public interface IfElseTree extends StatementTree {

    ExpressionTree getCondition();

    StatementTree getThenStatement();

    StatementTree getElseStatement();

}
