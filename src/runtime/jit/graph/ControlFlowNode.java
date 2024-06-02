package runtime.jit.graph;

public interface ControlFlowNode extends FlowNode {

    ControlFlowContext getContext();

    <P, R> R accept(FlowWalker<P, R> walker, P p);
}
