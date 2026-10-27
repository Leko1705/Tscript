package com.tscript.runtime.jit.comp.levels;

import com.tscript.runtime.jit.comp.optim.*;

import java.util.List;

public record L2(int threshold) implements OptimizingLevel {

    private static final List<IROptimizer> optimizers = List.of(
            new GlobalPromotion(),
            new ScalarReplacement(),
            new Canonicalizer(),
            new GuardHoisting(),
            new GuardEliminator(),
            new DeadCodeEliminator()
    );

    @Override
    public List<IROptimizer> getOptimizers() {
        return optimizers;
    }

    static List<IROptimizer> optimizers() {
        return optimizers;
    }
}
