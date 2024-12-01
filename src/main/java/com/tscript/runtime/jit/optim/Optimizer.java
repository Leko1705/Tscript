package com.tscript.runtime.jit.optim;

import com.tscript.runtime.jit.graph.Node;

public interface Optimizer {

    Node optimize(Node graph);

    boolean isOptimized();

}
