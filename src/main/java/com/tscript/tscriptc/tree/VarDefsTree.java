package com.tscript.tscriptc.tree;

import java.util.List;

public interface VarDefsTree extends StatementTree {

    List<? extends VarDefTree> getDefinitions();

}
