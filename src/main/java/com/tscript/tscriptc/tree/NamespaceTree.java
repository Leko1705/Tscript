package com.tscript.tscriptc.tree;

import java.util.List;

public interface NamespaceTree extends DefinitionTree {

    ModifiersTree getModifiers();

    String getName();

    List<? extends DefinitionTree> getDefinitions();

    List<? extends StatementTree> getStatements();

}
