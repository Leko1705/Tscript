package com.tscript.tscriptc.tree;

import java.util.List;

public interface ConstructorTree extends Tree {

    ModifiersTree getModifiers();

    List<? extends ParameterTree> getParameters();

    List<? extends ExpressionTree> getSuperArguments();

    BlockTree getBody();

}
