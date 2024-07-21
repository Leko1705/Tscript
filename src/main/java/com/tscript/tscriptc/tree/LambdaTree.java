package com.tscript.tscriptc.tree;

import java.util.List;

public interface LambdaTree extends ExpressionTree {

    List<? extends ClosureTree> getClosures();

    List<? extends ParameterTree> getParameters();

    BlockTree getBody();

}
