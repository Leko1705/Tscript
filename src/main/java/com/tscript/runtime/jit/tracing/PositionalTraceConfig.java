package com.tscript.runtime.jit.tracing;

import com.tscript.runtime.core.TThread;
import com.tscript.runtime.jit.JITTask;
import com.tscript.runtime.jit.NativeImage;
import com.tscript.runtime.jit.compilers.tasks.CompileTask;
import com.tscript.runtime.jit.graph.Node;
import com.tscript.runtime.typing.Type;

import java.util.function.Consumer;

public class PositionalTraceConfig implements TracingConfiguration {

    private final TThread thread;
    private final Profiler.Value value;
    private final int jumpBackAddress;
    private final boolean returns;

    public PositionalTraceConfig(TThread thread, Profiler.Value value, int ip, boolean returns) {
        this.thread = thread;
        this.value = value;
        this.jumpBackAddress = ip;
        this.returns = returns;
    }

    @Override
    public boolean isReturnComplete() {
        return false;
    }

    @Override
    public boolean isLoopComplete() {
        return thread.frameStack.element().ip == jumpBackAddress;
    }

    @Override
    public boolean doesReturn() {
        return returns;
    }

    @Override
    public JITTask getCompletionTask(Node node) {
        Consumer<NativeImage> c = img -> value.image = img;

        if (returns)
            return new CompileTask(node, new Type[0], c);
        else
            return new CompileTask(node, c);
    }

    @Override
    public int paramStorageSkips() {
        return 0;
    }
}
