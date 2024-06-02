package runtime.jit.graph;

public interface LinkedFlowNode extends FlowNode {

    FlowNode getNext();

    void setNext(FlowNode next);

    <P, R> R accept(FlowWalker<P, R> walker, P p);
}
