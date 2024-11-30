package com.tscript.compiler.impl.generation.writers;

import com.tscript.compiler.impl.generation.compiled.CompiledClass;

public interface ClassWriter {

    void writeClass(CompiledClass compiledClass);
}
