package runtime.jit.graph;

public interface FlowNode {

    <P, R> R accept(FlowWalker<P, R> walker, P p);

    default <P, R> R accept(FlowWalker<P, R> walker){
        return accept(walker, null);
    }

}
