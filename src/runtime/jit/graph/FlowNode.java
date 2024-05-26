package runtime.jit.graph;

public interface FlowNode {

    <R, P> R accept(FlowWalker<R, P> walker, P param);

    void setNext(FlowNode next);

    FlowNode getNext();

}
