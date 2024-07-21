package com.tscript.tscriptc.tree;

public interface TryCatchTree extends StatementTree {

    StatementTree getTryStatement();

    VarDefTree getExceptionVariable();

    StatementTree getCatchStatement();

}
