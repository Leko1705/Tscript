package com.tscript.tscriptc.tree;

public interface DoWhileTree extends StatementTree {

    StatementTree getStatement();

    ExpressionTree getCondition();

}
