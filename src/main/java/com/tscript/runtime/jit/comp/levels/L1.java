package com.tscript.runtime.jit.comp.levels;

import com.tscript.runtime.jit.comp.optim.*;

import java.util.List;

/**
 * The first compilation level. It runs the cheap optimizations that do not depend on
 * side traces; with {@code optimize == false} the graph is emitted as built.
 */
public record L1(int threshold, boolean optimize) implements OptimizingLevel {

    private static final List<IROptimizer> optimizers = List.of(
            new GlobalPromotion(),
            new ScalarReplacement(),
            new ObjectScalarReplacement(),
            new Canonicalizer(),
            new GuardHoisting(),
            new GuardEliminator(),
            new DeadCodeEliminator()
    );

    public L1(int threshold) {
        this(threshold, true);
    }

    @Override
    public List<IROptimizer> getOptimizers() {
        return optimize ? optimizers : List.of();
    }

    @Override
    public boolean stitchSideTraces() {
        return false;
    }
}
