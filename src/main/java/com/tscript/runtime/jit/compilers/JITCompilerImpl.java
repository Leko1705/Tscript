package com.tscript.runtime.jit.compilers;

import com.tscript.runtime.jit.JITCompiler;
import com.tscript.runtime.jit.NativeImage;
import com.tscript.runtime.jit.graph.Node;
import com.tscript.runtime.jit.lower.Generator;
import com.tscript.runtime.jit.lower.jbc.JBCGenerator;
import com.tscript.runtime.jit.optim.*;
import com.tscript.runtime.jit.optim.post.PostOptimizer;
import com.tscript.runtime.jit.optim.post.ToJBCTransformer;
import com.tscript.runtime.typing.Type;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class JITCompilerImpl implements JITCompiler {

    private static final int MAX_OPTIMIZATION_PASSES = 100;


    @Override
    public CompletableFuture<NativeImage> compile(Node graph, Type[] inputTypes) {
        graph = optimize(graph, inputTypes);
        graph = postOptimize(graph, inputTypes);
        NativeImage image = lower(graph);
        return CompletableFuture.completedFuture(image);
    }

    private Node optimize(Node node, Type[] inputTypes) {
        boolean optimized;
        int passes = 0;

        do {
            optimized = false;

            for (Optimizer optimizer : getOptimizers()) {
                node = optimizer.optimize(node, inputTypes);
                optimized |= optimizer.isOptimized();
            }
            passes++;
        }
        while (optimized && passes < MAX_OPTIMIZATION_PASSES);

        return node;
    }

    private Node postOptimize(Node node, Type[] inputTypes) {
        for (PostOptimizer postOptimizer : getPostOptimizers()) {
            node = postOptimizer.optimize(node, inputTypes);
        }
        return node;
    }

    private List<Optimizer> getOptimizers() {
        return List.of(
                new ConstantFolder(false), new DeadVariableEliminator(),
                new UnsafeReducer(), new TrivialCallRemover(), new TypeReducer());
    }

    private List<PostOptimizer> getPostOptimizers() {
        return List.of(new ToJBCTransformer(), new UnsafeReducer());
    }

    private NativeImage lower(Node graph) {
        Generator generator = new JBCGenerator();
        return generator.generate(graph);
    }

}
