package com.tscript.compiler.impl.generation.writers;

import com.tscript.compiler.impl.generation.compiled.CompiledFunction;

public interface FunctionWriter {

    void writeFunction(CompiledFunction function);

}
