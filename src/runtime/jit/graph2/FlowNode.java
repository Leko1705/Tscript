package runtime.jit.graph2;

public interface FlowNode {

    <P, R> R accept(FlowWalker<P, R> walker, P p);

}
