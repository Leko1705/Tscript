package com.tscript.runtime.jit.comp.levels;

import com.tscript.runtime.jit.*;
import com.tscript.runtime.jit.comp.ir.Graph;
import com.tscript.runtime.jit.comp.optim.IROptimizer;

import java.util.List;

public interface OptimizingLevel extends CompilationLevel {

    int threshold();

    List<IROptimizer> getOptimizers();


    default int optimizationLimit() {
        return 100;
    }

    @Override
    default CompileTask prepare(Graph graph) {
        return c -> {
            Graph optimizedGraph = Optimizer.optimize(graph, optimizationLimit(), getOptimizers());
            JITConfiguration config = c.getConfiguration();
            return config.emitter().emit(c, optimizedGraph);
        };
    }

}
