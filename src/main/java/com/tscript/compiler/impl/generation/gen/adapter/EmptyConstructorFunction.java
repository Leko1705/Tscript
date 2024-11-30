package com.tscript.compiler.impl.generation.gen.adapter;

import com.tscript.compiler.impl.utils.TCTree;
import com.tscript.compiler.source.tree.Modifier;
import com.tscript.compiler.source.utils.Location;

import java.util.List;
import java.util.Set;

public class EmptyConstructorFunction extends TCTree.TCFunctionTree {
    public EmptyConstructorFunction(Location location, String className) {
        super(location,
                className + "@constructor",
                new TCModifiersTree(location, Set.of(Modifier.PUBLIC)),
                List.of(),
                new TCBlockTree(location, List.of(
                        new TCReturnTree(location, new TCThisTree(location))
                )));
    }
}
