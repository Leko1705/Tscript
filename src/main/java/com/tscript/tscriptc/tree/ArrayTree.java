package com.tscript.tscriptc.tree;

import java.util.List;

public interface ArrayTree extends ExpressionTree {

    List<? extends ExpressionTree> getContents();

}
