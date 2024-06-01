package runtime.jit.graph.build;

import runtime.jit.graph.FlowNode;

import java.util.HashMap;
import java.util.Map;

public class NodeMapImpl implements NodeMap {

    private final Map<FlowNode, Integer> node2int = new HashMap<>();
    private final Map<Integer, FlowNode> int2node = new HashMap<>();

    @Override
    public void add(FlowNode node, int index) {
        node2int.put(node, index);
        int2node.put(index, node);
    }

    @Override
    public FlowNode get(int index) {
        return int2node.get(index);
    }

    @Override
    public int get(FlowNode node) {
        return node2int.get(node);
    }
}
