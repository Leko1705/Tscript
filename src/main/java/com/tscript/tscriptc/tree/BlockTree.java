package com.tscript.tscriptc.tree;

import java.util.List;

public interface BlockTree extends StatementTree {

    List<? extends StatementTree> getStatements();

}
