package com.tscript.tscriptc.tree;

public interface WhileDoTree extends StatementTree {

    ExpressionTree getCondition();

    StatementTree getStatement();

}
