package tscriptc.util;

import tscriptc.tree.Tree;

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
