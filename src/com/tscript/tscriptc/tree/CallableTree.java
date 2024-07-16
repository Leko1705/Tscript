package com.tscript.tscriptc.tree;

import java.util.List;

@InheritOnly
public interface CallableTree extends DefinitionTree {

    List<ParameterTree> getParameters();

}
