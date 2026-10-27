package com.tscript.runtime.jit.comp.levels;

import com.tscript.runtime.jit.comp.ir.Graph;
import com.tscript.runtime.jit.comp.optim.IROptimizer;

import java.util.List;

public class Optimizer {

    public static Graph optimize(Graph graph, int limit, List<IROptimizer> optimizers) {
        for (int i = 0; i < limit; i++) {
            boolean optimized = false;
            graph.typeMap().flush();

            for (IROptimizer optimizer : optimizers) {
                optimized |= optimizer.optimize(graph);
            }

            if (!optimized) break;
        }

        return graph;
    }

}
