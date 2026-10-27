package com.tscript.runtime.jit;

import com.tscript.runtime.jit.comp.ir.Graph;

public interface CompilationLevel {

    int threshold();

    CompileTask prepare(Graph graph);

}
