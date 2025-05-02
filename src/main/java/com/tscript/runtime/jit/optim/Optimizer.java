package com.tscript.runtime.jit.optim;

import com.tscript.runtime.jit.graph.Node;
import com.tscript.runtime.jit.ir.IRBlock;
import com.tscript.runtime.typing.Type;

public interface Optimizer {

    default IRBlock optimize(IRBlock block, Type[] inputTypes) {
        return block;
    }

    default Node optimize(Node graph, Type[] inputTypes){
        return optimize(graph);
    }

    Node optimize(Node graph);

    boolean isOptimized();

}
