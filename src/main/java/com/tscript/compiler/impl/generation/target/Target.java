package com.tscript.compiler.impl.generation.target;

import com.tscript.compiler.impl.generation.compiled.CompiledFile;

public interface Target {

    void write(CompiledFile file);

}
