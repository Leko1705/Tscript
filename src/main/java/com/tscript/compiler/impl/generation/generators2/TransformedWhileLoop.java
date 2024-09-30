package com.tscript.compiler.impl.generation.generators2;

import com.tscript.compiler.impl.utils.TCTree;

public class TransformedWhileLoop extends TCTree.TCIfElseTree {

    public TransformedWhileLoop(TCWhileDoTree tree) {
        super(tree.location,
                tree.condition,
                new TCDoWhileTree(
                        tree.location,
                        tree.statement,
                        tree.condition
                ),
                null);
    }

}
