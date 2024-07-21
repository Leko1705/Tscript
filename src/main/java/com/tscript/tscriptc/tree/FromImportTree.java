package com.tscript.tscriptc.tree;

import java.util.List;

public interface FromImportTree extends StatementTree {

    List<String> getFromAccessChain();

    List<String> getImportAccessChain();

}
