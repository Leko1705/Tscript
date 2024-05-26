package runtime.jit.optim;

import runtime.jit.graph.FlowNode;

public interface Optimizer {

    FlowNode optimize(FlowNode graph);

}
