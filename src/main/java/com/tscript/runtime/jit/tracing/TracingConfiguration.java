package com.tscript.runtime.jit.tracing;

import com.tscript.runtime.jit.JITTask;
import com.tscript.runtime.jit.graph.Node;

public interface TracingConfiguration {

    boolean isReturnComplete();

    boolean isLoopComplete();

    boolean doesReturn();

    JITTask getCompletionTask(Node node);

    int paramStorageSkips();

}
