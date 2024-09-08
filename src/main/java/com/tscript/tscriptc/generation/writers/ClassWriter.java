package com.tscript.tscriptc.generation.writers;

import com.tscript.tscriptc.generation.compiled.CompiledClass;

public interface ClassWriter {

    void writeClass(CompiledClass compiledClass);
}
