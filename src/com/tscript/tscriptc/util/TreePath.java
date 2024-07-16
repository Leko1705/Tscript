package com.tscript.tscriptc.util;

import com.tscript.tscriptc.tree.Tree;

public class TreePath {

    private TreePath parent;
    private TreePath child;

    private final Tree tree;

    public Tree getTree() {
        return tree;
    }

    public TreePath(Tree tree){
        this.tree = tree;
    }

}
