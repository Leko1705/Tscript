package com.tscript.runtime.jit.tracing;

import com.tscript.runtime.core.TThread;
import com.tscript.runtime.jit.JITTask;
import com.tscript.runtime.jit.NativeImage;
import com.tscript.runtime.jit.caching.JITCache;
import com.tscript.runtime.jit.compilers.tasks.CompileTask2;
import com.tscript.runtime.jit.ir.IRBlock;

import java.util.function.Consumer;

public class LoopTracingConfiguration implements TracerConfiguration {

    private final String context;
    private final int initialFunctionStackIndex;
    private final int loopHeadIndex;

    public LoopTracingConfiguration(String context,int initialFunctionStackIndex, int loopHeadIndex) {
        this.context = context;
        this.initialFunctionStackIndex = initialFunctionStackIndex;
        this.loopHeadIndex = loopHeadIndex;
    }

    @Override
    public boolean isFunctionFocused() {
        return false;
    }

    @Override
    public boolean isTraceComplete(TThread thread) {
        return inInitialFunction(thread) && onLoopHead(thread);
    }

    @Override
    public boolean isParameterStorage() {
        return false;
    }

    @Override
    public JITTask completeTrace(TThread thread, IRBlock block) {
        JITCache cache = thread.getVM().getJitCompiler().getCache();

        Consumer<NativeImage> compilationCompletionObserver =
                image -> cache.putPositional(context, loopHeadIndex, image);

        return new CompileTask2(block, compilationCompletionObserver);
    }


    private boolean onLoopHead(TThread thread) {
        return thread.frameStack.element().ip + 1 == loopHeadIndex;
    }

    private boolean inInitialFunction(TThread thread) {
        return thread.frameStack.size() == initialFunctionStackIndex;
    }
}
