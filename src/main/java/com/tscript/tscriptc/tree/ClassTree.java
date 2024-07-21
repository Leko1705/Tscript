package com.tscript.tscriptc.tree;

import java.util.List;

public interface ClassTree extends DefinitionTree, StatementTree {

    ModifiersTree getModifiers();

    String getName();

    String getSuperName();

    ConstructorTree getConstructor();

    List<? extends DefinitionTree> getMembers();

}
