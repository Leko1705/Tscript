package com.tscript.runtime.jit.comp.levels;

import com.tscript.runtime.jit.comp.optim.*;

import java.util.List;

public record L2(int threshold) implements OptimizingLevel {

    private static final List<IROptimizer> optimizers = List.of(
            new ConditionalConstantPropagator(),
            new GuardEliminator(),
            new EscapeAnalysis()
    );

    @Override
    public List<IROptimizer> getOptimizers() {
        return optimizers;
    }
}
