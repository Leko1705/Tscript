package runtime.jit.graph2;

public interface FlowWalker<P, R> {

    R visitBranchNode(FlowNode branchNode, P p);

    R visitGotoNode(FlowNode gotoNode, P p);

    R visitPathEndNode(FlowNode pathEndNode, P p);

}
