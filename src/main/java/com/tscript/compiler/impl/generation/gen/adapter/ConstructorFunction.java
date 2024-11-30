package com.tscript.compiler.impl.generation.gen.adapter;

import com.tscript.compiler.impl.utils.TCTree;

public class ConstructorFunction extends TCTree.TCFunctionTree {

    public ConstructorFunction(TCConstructorTree constructor, String className) {
        super(constructor.location,
                className + "@constructor",
                constructor.modifiers,
                constructor.parameters,
                constructor.body);
    }
}
