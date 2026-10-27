package com.tscript.runtime.jit.comp.levels;

import com.tscript.runtime.jit.*;
import com.tscript.runtime.jit.comp.ir.Graph;

public record L1(int threshold) implements CompilationLevel {

    @Override
    public CompileTask prepare(Graph graph) {
        return new L1Task(graph);
    }

    private record L1Task(Graph graph) implements CompileTask {
        @Override
        public NativeImage run(JITCompiler compiler) {
            JITConfiguration config = compiler.getConfiguration();
            return config.emitter().emit(compiler, graph);
        }
    }


}
