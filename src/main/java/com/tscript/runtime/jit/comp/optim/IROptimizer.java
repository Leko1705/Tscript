package com.tscript.runtime.jit.comp.optim;

import com.tscript.runtime.jit.comp.ir.Graph;

public interface IROptimizer {

    boolean optimize(Graph graph);

}
