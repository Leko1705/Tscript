package com.tscript.compiler.impl.generation;

import com.tscript.compiler.impl.generation.compiled.CompiledFile;
import com.tscript.compiler.impl.generation.gen.FileGenerator;
import com.tscript.compiler.impl.utils.TCTree;

public class Generator {

    public static CompiledFile generate(TCTree tree) {
        FileGenerator generator = new FileGenerator();
        tree.accept(generator);
        return generator.getCompiled();
    }

}
