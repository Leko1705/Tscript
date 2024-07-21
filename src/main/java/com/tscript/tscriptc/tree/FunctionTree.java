package com.tscript.tscriptc.tree;

import java.util.List;

public interface FunctionTree extends DefinitionTree, StatementTree {

    ModifiersTree getModifiers();

    String getName();

    List<? extends ParameterTree> getParameters();

    BlockTree getBody();

}
