package com.tscript.tscriptc.tree;

public interface ForLoopTree extends StatementTree {

    VarDefTree getVariable();

    ExpressionTree getIterable();

    StatementTree getBody();

}
