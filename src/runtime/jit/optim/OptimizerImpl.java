package runtime.jit.optim;

import runtime.jit.graph.FlowNode;
import runtime.jit.optim.techniques.ConstantFolder;
import runtime.jit.optim.techniques.Technique;

public class OptimizerImpl implements Optimizer {

    private boolean optimizationPerformed = false;

    @Override
    public FlowNode optimize(FlowNode graph) {

        do {
            optimizationPerformed = false;

            performOptimization(graph, new ConstantFolder());

        }while (optimizationPerformed);

        return graph;
    }

    private void performOptimization(FlowNode graph, Technique<?, ?> technique) {
        graph.accept(technique, null);
        optimizationPerformed |= technique.optimizationPerformed();
    }
}
