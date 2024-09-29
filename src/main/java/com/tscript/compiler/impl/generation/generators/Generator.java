package com.tscript.compiler.impl.generation.generators;

import com.tscript.compiler.impl.generation.compiled.CompiledFile;
import com.tscript.compiler.source.tree.Tree;

public class Generator {

    public static CompiledFile generate(Tree tree) {
        FileGenerator generator = new FileGenerator();
        tree.accept(generator);
        return generator.getCompiled();
    }

}
