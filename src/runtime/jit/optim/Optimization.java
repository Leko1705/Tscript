package runtime.jit.optim;

import runtime.jit.graph.FlowNode;
import runtime.jit.graph.FlowWalker;
import runtime.jit.utils.TreeGraphWalker;

public class Optimization<P, R> extends TreeGraphWalker<P, R> implements Optimizer {

    private boolean optimized = false;

    public Optimization(FlowWalker<P, R> walker) {
        super(walker);
    }

    public boolean optimizationPerformed(){
        return optimized;
    }

    public void notifyOptimizationPerformed(){
        optimized = true;
    }

    @Override
    public FlowNode optimize(FlowNode graph) {
        graph.accept(this, null);
        return graph;
    }

}
