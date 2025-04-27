package com.tscript.runtime.jit.tracing;

import com.tscript.runtime.core.TThread;
import com.tscript.runtime.jit.JITCompiler;
import com.tscript.runtime.jit.JITTask;
import com.tscript.runtime.jit.NativeImage;
import com.tscript.runtime.jit.caching.JITCache;
import com.tscript.runtime.jit.compilers.tasks.CompileTask;
import com.tscript.runtime.jit.graph.Node;
import com.tscript.runtime.typing.Type;

import java.util.function.Consumer;

public class PositionalTraceConfig implements TracingConfiguration {

    private final TThread thread;
    private final JITCompiler compiler;
    private final int jumpBackAddress;
    private final boolean returns;
    private final String context;

    public PositionalTraceConfig(TThread thread, JITCompiler compiler, String context, int jumpBackAddress, boolean returns) {
        this.thread = thread;
        this.compiler = compiler;
        this.context = context;
        this.jumpBackAddress = jumpBackAddress;
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
        JITCache cache = compiler.getCache();
        Consumer<NativeImage> c = img -> cache.putPositional(context, jumpBackAddress, img);

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
