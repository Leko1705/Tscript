package com.tscript.runtime.jit.optim.post;

import com.tscript.runtime.jit.graph.Node;
import com.tscript.runtime.typing.Type;

public interface PostOptimizer {

    Node optimize(Node graph, Type[] inputTypes);
}
