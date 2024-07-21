package com.tscript.tscriptc.tree;

import java.util.List;

public interface ImportTree extends StatementTree {

    List<String> getAccessChain();

}
