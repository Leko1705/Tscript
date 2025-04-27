package com.tscript.runtime.jit.tracing;

import com.tscript.runtime.core.TThread;
import com.tscript.runtime.core.VirtualFunction;
import com.tscript.runtime.jit.JITCompiler;
import com.tscript.runtime.jit.JITTask;
import com.tscript.runtime.jit.NativeImage;
import com.tscript.runtime.jit.caching.JITCache;
import com.tscript.runtime.jit.compilers.tasks.CompileTask;
import com.tscript.runtime.jit.graph.Node;
import com.tscript.runtime.typing.Type;

import java.util.function.Consumer;

public class FunctionTraceConfig implements TracingConfiguration {

    private final TThread thread;
    private final int callStackDepth;
    private final Type[] argTypes;
    private final JITCompiler compiler;
    private final VirtualFunction function;

    public FunctionTraceConfig(TThread thread,
                               JITCompiler compiler,
                               VirtualFunction function,
                               int callStackDepth,
                               Type[] argTypes) {
        this.thread = thread;
        this.compiler = compiler;
        this.function = function;
        this.callStackDepth = callStackDepth;
        this.argTypes = argTypes;
    }

    @Override
    public boolean isReturnComplete() {
        return thread.frameStack.size() == callStackDepth;
    }

    @Override
    public boolean isLoopComplete() {
        return false;
    }

    @Override
    public boolean doesReturn() {
        return true;
    }

    @Override
    public JITTask getCompletionTask(Node node) {
        JITCache cache = compiler.getCache();
        Consumer<NativeImage> observer = img -> cache.putFunctional(function, argTypes, img);
        return new CompileTask(node, argTypes, observer);
    }

    @Override
    public int paramStorageSkips() {
        return argTypes.length;
    }
}
