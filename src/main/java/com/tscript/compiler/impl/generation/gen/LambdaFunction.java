package com.tscript.compiler.impl.generation.gen;

import com.tscript.compiler.impl.utils.TCTree;

import java.util.Set;

public class LambdaFunction extends TCTree.TCFunctionTree {
    public LambdaFunction(TCLambdaTree tree, String name) {
        super(
                tree.getLocation(),
                name,
                new TCModifiersTree(tree.getLocation(), Set.of()),
                tree.parameters,
                tree.body);
    }
}
