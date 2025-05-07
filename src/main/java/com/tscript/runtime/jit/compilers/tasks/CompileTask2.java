package com.tscript.runtime.jit.compilers.tasks;

import com.tscript.runtime.jit.JITTask;
import com.tscript.runtime.jit.NativeImage;
import com.tscript.runtime.jit.emit.Emitter;
import com.tscript.runtime.jit.emit.jbc.JBCEmitter;
import com.tscript.runtime.jit.ir.IRBlock;
import com.tscript.runtime.jit.optim.v2.*;
import com.tscript.runtime.typing.Type;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class CompileTask2 implements JITTask {

    private static final int MAX_OPTIMIZATION_PASSES = 100;
    private static final Type[] EMPTY_TYPE_ARRAY = new Type[0];

    private final IRBlock root;
    private final Type[] argTypes;
    private final Consumer<NativeImage> completionObserver;

    public CompileTask2(IRBlock root, Consumer<NativeImage> completionObserver) {
        this(root, EMPTY_TYPE_ARRAY, completionObserver);
    }

    public CompileTask2(IRBlock root, Type[] argTypes, Consumer<NativeImage> completionObserver) {
        this.root = root;
        this.argTypes = argTypes;
        this.completionObserver = completionObserver;
    }

    @Override
    public void run() {
        if (argTypes == null) return;
        IRBlock block = optimize(root, argTypes);
        NativeImage image = emit(block);
        completionObserver.accept(image);
    }

    private IRBlock optimize(IRBlock block, Type[] inputTypes) {
        boolean optimized;
        int passes = 0;

        do {
            optimized = false;

            for (IROptimizer optimizer : getOptimizers()) {
                block = optimizer.optimize(block, inputTypes);
                Objects.requireNonNull(block);
                optimized |= optimizer.isOptimized();
            }
            passes++;
        }
        while (optimized && passes < MAX_OPTIMIZATION_PASSES);

        return block;
    }

    private List<IROptimizer> getOptimizers() {
        return List.of(
                new TypeInferrer(), new WrapOptimizer(), new ConstantFolder(),
                new UnsafeReducer(), new DeadVariableEliminator(), new DeadCodeEliminator()
        );
    }

    private NativeImage emit(IRBlock block) {
        Emitter emitter = new JBCEmitter();
        return emitter.emit(block);
    }
}
