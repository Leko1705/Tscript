package com.tscript.tscriptc.tree;


public interface DefinitionTree extends Tree {

    ModifiersTree getModifiers();

    String getName();
}
