package com.tscript.tscriptc.tree;

public interface ParameterTree extends DefinitionTree {

    ModifiersTree getModifiers();

    String getName();

    ExpressionTree getDefaultValue();

}
