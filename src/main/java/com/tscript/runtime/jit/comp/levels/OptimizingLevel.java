package com.tscript.runtime.jit.comp.levels;

import com.tscript.runtime.jit.*;
import com.tscript.runtime.jit.comp.ir.Graph;
import com.tscript.runtime.jit.comp.ir.IREffect;
import com.tscript.runtime.jit.comp.ir.nodes.Guard;
import com.tscript.runtime.jit.comp.optim.IROptimizer;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

/**
 * A compilation level that optimizes the graph before it is emitted. If side trace stitching is
 * enabled, the compiled side traces attached to the guards of the graph are optimized as well and
 * emitted in place of the guard failure paths (trace trees).
 */
public interface OptimizingLevel extends CompilationLevel {

    int threshold();

    List<IROptimizer> getOptimizers();

    default int optimizationLimit() {
        return 100;
    }

    /**
     * @return true if compiled side traces are stitched into the compiled code
     */
    default boolean stitchSideTraces() {
        return true;
    }

    /**
     * @return the maximum nesting of stitched side traces
     */
    default int maxStitchDepth() {
        return 4;
    }

    @Override
    default CompileTask prepare(Graph graph) {
        return c -> {
            Graph optimizedGraph = Optimizer.optimize(graph, optimizationLimit(), getOptimizers());
            if (stitchSideTraces()) {
                Function<Object, Graph> sideTraces = exit -> c.imageCache().sideTraceGraph(exit);
                prepareSideTraces(optimizedGraph, sideTraces, 0, Collections.newSetFromMap(new IdentityHashMap<>()));
            }
            else {
                optimizedGraph.setSideTraces(null);
            }
            JITConfiguration config = c.getConfiguration();
            return config.emitter().emit(c, optimizedGraph);
        };
    }

    /**
     * @return the number of compiled side traces that would be stitched into the code of the graph
     * (if this number did not change since the last compilation, recompiling is pointless)
     */
    default int countSideTraces(Graph graph, Function<Object, Graph> sideTraces) {
        if (!stitchSideTraces()) return 0;
        return countSideTraces(graph, sideTraces, 0, Collections.newSetFromMap(new IdentityHashMap<>()));
    }

    private int countSideTraces(Graph graph, Function<Object, Graph> sideTraces, int depth, Set<Graph> visited) {
        if (depth >= maxStitchDepth() || !visited.add(graph)) return 0;
        int count = 0;
        for (IREffect effect : graph.effects()) {
            if (effect instanceof Guard guard && guard.getDeoptInfo() != null) {
                Graph side = sideTraces.apply(guard.getDeoptInfo());
                if (side != null && !visited.contains(side)) {
                    count += 1 + countSideTraces(side, sideTraces, depth + 1, visited);
                }
            }
        }
        return count;
    }

    private void prepareSideTraces(Graph graph, Function<Object, Graph> sideTraces, int depth, Set<Graph> visited) {
        graph.setSideTraces(sideTraces);
        if (depth >= maxStitchDepth() || !visited.add(graph)) return;
        for (IREffect effect : graph.effects()) {
            if (effect instanceof Guard guard) {
                Graph side = graph.sideTrace(guard.getDeoptInfo());
                if (side != null && !visited.contains(side)) {
                    Optimizer.optimize(side, optimizationLimit(), getOptimizers());
                    prepareSideTraces(side, sideTraces, depth + 1, visited);
                }
            }
        }
    }

}
