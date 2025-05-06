package com.tscript.runtime.jit.tracing;

import com.tscript.runtime.core.TThread;
import com.tscript.runtime.jit.JITTask;
import com.tscript.runtime.jit.NativeImage;
import com.tscript.runtime.jit.caching.JITCache;
import com.tscript.runtime.jit.compilers.tasks.CompileTask2;
import com.tscript.runtime.jit.ir.IRBlock;
import com.tscript.runtime.typing.Type;

import java.util.function.Consumer;

public class FunctionTracingConfiguration implements TracerConfiguration {

    private final String context;
    private final int initialFunctionStackIndex;
    private final Type[] parameterTypes;
    private int params;

    public FunctionTracingConfiguration(String context, Type[] paramTypes, int initialFunctionStackIndex, int params) {
        this.context = context;
        this.initialFunctionStackIndex = initialFunctionStackIndex;
        this.parameterTypes = paramTypes;
        this.params = params;
    }

    @Override
    public boolean isFunctionFocused() {
        return true;
    }

    @Override
    public boolean isTraceComplete(TThread thread) {
        return thread.frameStack.size() == initialFunctionStackIndex;
    }

    @Override
    public boolean isParameterStorage() {
        return (params--) > 0;
    }

    @Override
    public JITTask completeTrace(TThread thread, IRBlock block) {
        JITCache cache = thread.getVM().getJitCompiler().getCache();

        Consumer<NativeImage> compilationCompletionObserver =
                image -> cache.putFunctional(context, parameterTypes, image);

        return new CompileTask2(block, parameterTypes, compilationCompletionObserver);
    }
}
