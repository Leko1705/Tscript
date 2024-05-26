package runtime.jit.gen;

import runtime.jit.graph.FlowNode;
import runtime.type.Callable;

public interface Generator {

    Callable generate(FlowNode graph);

}
