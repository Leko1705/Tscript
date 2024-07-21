package com.tscript.tscriptc.tree;

import java.util.List;

public interface ModuleTree extends Tree {

    String getName();

    List<? extends DefinitionTree> getDefinitions();

    List<? extends StatementTree> getStatements();

}
