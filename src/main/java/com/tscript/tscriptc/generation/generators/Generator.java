package com.tscript.tscriptc.generation.generators;

import com.tscript.tscriptc.analyze.scoping.Scope;
import com.tscript.tscriptc.generation.compiled.CompiledFile;
import com.tscript.tscriptc.tree.Tree;

public class Generator {

    public static CompiledFile generate(Tree tree, Scope scope) {
        FileGenerator generator = new FileGenerator();
        tree.accept(generator, scope);
        return generator.getCompiled();
    }

}
