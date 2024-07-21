package com.tscript.tscriptc.tree;

import java.util.List;

public interface DictionaryTree extends ExpressionTree {

    List<? extends ExpressionTree> getKeys();

    List<? extends ExpressionTree> getValues();

}
