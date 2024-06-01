package runtime.jit.graph.build;

import runtime.jit.graph.FlowNode;

public interface NodeMap {

    void add(FlowNode node, int index);

    FlowNode get(int index);

    int get(FlowNode node);

}
