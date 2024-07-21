package com.tscript.tscriptc.tree;

public interface VarDefTree extends DefinitionTree {

    ModifiersTree getModifiers();

    String getName();
}
