package runtime.jit.graph;

public interface ExpressionOwner extends FlowNode {

    void replace(ExpressionNode old, ExpressionNode newNode);

}
