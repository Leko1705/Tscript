package com.tscript.tscriptc.tree;

import java.util.List;

public interface CallTree extends ExpressionTree {

    ExpressionTree getCalled();

    List<? extends ArgumentTree> getArguments();

}
