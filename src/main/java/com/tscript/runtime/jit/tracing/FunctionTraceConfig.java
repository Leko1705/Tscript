package com.tscript.runtime.jit.tracing;

import com.tscript.runtime.core.TThread;
import com.tscript.runtime.jit.JITTask;
import com.tscript.runtime.jit.compilers.tasks.CompileTask;
import com.tscript.runtime.jit.graph.Node;
import com.tscript.runtime.typing.Type;

public class FunctionTraceConfig implements TracingConfiguration {

    private final TThread thread;
    private final int callStackDepth;
    private final Type[] argTypes;
    private final Profiler.Value value;

    public FunctionTraceConfig(TThread thread,
                               int callStackDepth,
                               Type[] argTypes,
                               Profiler.Value value) {
        this.thread = thread;
        this.callStackDepth = callStackDepth;
        this.argTypes = argTypes;
        this.value = value;
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
        return new CompileTask(node, argTypes, img -> value.image = img);
    }

    @Override
    public int paramStorageSkips() {
        return argTypes.length;
    }
}
