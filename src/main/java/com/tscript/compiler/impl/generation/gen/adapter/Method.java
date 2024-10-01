package com.tscript.compiler.impl.generation.gen.adapter;

import com.tscript.compiler.impl.utils.TCTree;

public class Method extends TCTree.TCFunctionTree {
    public Method(TCFunctionTree f, String className) {
        super(f.location,
                className + "." + f.name,
                f.modifiers,
                f.parameters,
                f.body);
        sym = f.sym;
    }
}
