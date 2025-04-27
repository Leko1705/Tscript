package com.tscript.runtime.jit.compilers.tasks;

import com.tscript.runtime.jit.JITTask;
import com.tscript.runtime.jit.NativeImage;
import com.tscript.runtime.jit.graph.Node;
import com.tscript.runtime.jit.lower.Generator;
import com.tscript.runtime.jit.lower.jbc.JBCGenerator;
import com.tscript.runtime.jit.optim.*;
import com.tscript.runtime.jit.optim.post.PostOptimizer;
import com.tscript.runtime.jit.optim.post.ToJBCTransformer;
import com.tscript.runtime.typing.Type;

import java.util.List;
import java.util.function.Consumer;

public class CompileTask implements JITTask {

    private static final int MAX_OPTIMIZATION_PASSES = 100;
    public static final Type[] EMPTY_TYPE_ARRAY = new Type[0];

    private final Node node;
    private final Type[] argTypes;
    private final Consumer<NativeImage> completionObserver;
    private final boolean returns;

    public CompileTask(Node node, Type[] argTypes, Consumer<NativeImage> completionObserver) {
        this.node = node;
        this.argTypes = argTypes;
        this.completionObserver = completionObserver;
        this.returns = true;
    }

    public CompileTask(Node node, Consumer<NativeImage> completionObserver){
        this.node = node;
        this.argTypes = EMPTY_TYPE_ARRAY;
        this.completionObserver = completionObserver;
        this.returns = false;
    }

    @Override
    public void run() {
        Node graph = this.node;
        Type[] argTypes = this.argTypes;

        graph = optimize(graph, argTypes);
        graph = postOptimize(graph, argTypes);
        NativeImage image = lower(graph);
        completionObserver.accept(image);
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
        Generator generator = new JBCGenerator(returns);
        return generator.generate(graph);
    }


}
