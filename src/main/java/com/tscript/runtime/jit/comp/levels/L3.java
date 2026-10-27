package com.tscript.runtime.jit.comp.levels;

import com.tscript.runtime.jit.comp.optim.IROptimizer;

import java.util.List;

/**
 * Recompiles the graph of a very hot entry once more, so that side traces that were compiled
 * after the entry reached {@link L2} are stitched as well (with a larger stitching depth).
 */
public record L3(int threshold) implements OptimizingLevel {

    @Override
    public List<IROptimizer> getOptimizers() {
        return L2.optimizers();
    }

    @Override
    public int maxStitchDepth() {
        return 8;
    }
}
