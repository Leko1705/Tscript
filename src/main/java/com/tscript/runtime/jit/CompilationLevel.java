package com.tscript.runtime.jit;

import com.tscript.runtime.jit.comp.ir.Graph;

public interface CompilationLevel {

    int threshold();

    CompileTask prepare(Graph graph);

    /**
     * @return true if this level records a new trace, false if it recompiles the graph recorded
     * by an earlier level (if there is one)
     */
    default boolean retrace() {
        return false;
    }

}
