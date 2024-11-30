package com.tscript.compiler.impl.generation.gen.adapter;

import com.tscript.compiler.impl.utils.TCTree;
import com.tscript.compiler.source.tree.Modifier;
import com.tscript.compiler.source.utils.Location;

import java.util.List;
import java.util.Set;

public class StaticBlock extends TCTree.TCFunctionTree {

    public StaticBlock(Location location, String classNme) {
        super(location,
                classNme + "@static",
                new TCModifiersTree(location, Set.of(Modifier.STATIC)),
                List.of(),
                new TCBlockTree(location, List.of()));
    }
}
