package runtime.jit.graph.build;

import runtime.jit.graph.FlowNode;

public interface GraphBuilder {

    FlowNode build(byte[][] code);
}
