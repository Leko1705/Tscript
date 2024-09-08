package com.tscript.tscriptc.generation.writers;

import com.tscript.tscriptc.generation.compiled.CompiledFunction;

public interface FunctionWriter {

    void writeFunction(CompiledFunction function);

}
