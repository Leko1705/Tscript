package com.tscript.runtime.jit.compilers;

import com.tscript.runtime.jit.JITCompiler;
import com.tscript.runtime.jit.NativeImage;
import com.tscript.runtime.jit.graph.Node;
import com.tscript.runtime.jit.lower.Generator;
import com.tscript.runtime.jit.lower.jbc.JBCGenerator;
import com.tscript.runtime.jit.optim.Optimizer;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class JITCompilerImpl implements JITCompiler {

    private static final int MAX_OPTIMIZATION_PASSES = 100;


    @Override
    public CompletableFuture<NativeImage> compile(Node graph) {
        graph = optimize(graph);
        NativeImage image = lower(graph);
        return CompletableFuture.completedFuture(image);
    }

    private Node optimize(Node node) {
        boolean optimized;
        int passes = 0;

        do {
            optimized = false;

            for (Optimizer optimizer : getOptimizers()) {
                node = optimizer.optimize(node);
                optimized |= optimizer.isOptimized();
            }
            passes++;
        }
        while (optimized && passes < MAX_OPTIMIZATION_PASSES);

        return node;
    }

    private List<Optimizer> getOptimizers() {
        return List.of();
    }

    private NativeImage lower(Node graph) {
        Generator generator = new JBCGenerator();
        return generator.generate(graph);
    }

}
