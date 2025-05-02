package com.tscript.runtime.jit.compilers.tasks;

import com.tscript.runtime.jit.JITTask;
import com.tscript.runtime.jit.NativeImage;
import com.tscript.runtime.jit.ir.IRBlock;
import com.tscript.runtime.jit.optim.*;
import com.tscript.runtime.typing.Type;

import java.util.List;
import java.util.function.Consumer;

public class CompileTask2 implements JITTask {

    private static final int MAX_OPTIMIZATION_PASSES = 100;
    private static final Type[] EMPTY_TYPE_ARRAY = new Type[0];

    private final IRBlock root;
    private final Type[] argTypes;
    private final Consumer<NativeImage> completionObserver;

    public CompileTask2(IRBlock root, Type[] argTypes, Consumer<NativeImage> completionObserver) {
        this.root = root;
        this.argTypes = argTypes;
        this.completionObserver = completionObserver;
    }

    @Override
    public void run() {
        IRBlock block = optimize(root, argTypes);
        NativeImage image = emit(block);
        completionObserver.accept(image);
    }

    private IRBlock optimize(IRBlock block, Type[] inputTypes) {
        boolean optimized;
        int passes = 0;

        do {
            optimized = false;

            for (Optimizer optimizer : getOptimizers()) {
                block = optimizer.optimize(block, inputTypes);
                optimized |= optimizer.isOptimized();
            }
            passes++;
        }
        while (optimized && passes < MAX_OPTIMIZATION_PASSES);

        return block;
    }

    private List<Optimizer> getOptimizers() {
        return List.of(
                new ConstantFolder(false), new DeadVariableEliminator(),
                new UnsafeReducer(), new TrivialCallRemover(), new TypeReducer());
    }

    private NativeImage emit(IRBlock block) {
        return null;
    }
}
