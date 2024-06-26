package runtime.jit.optim;

import runtime.jit.graph.FlowNode;
import runtime.jit.graph.FlowWalker;

import java.util.List;

public class OptimizerImpl implements Optimizer {

    private static final int MAX_OPTIMIZATION_PASSES = 100;

    private static List<FlowWalker<?, ?>> algorithms;


    private static void loadTechniques(){
        if (algorithms != null) return;
        algorithms = List.of(

        );
    }


    @Override
    public FlowNode optimize(FlowNode graph) {
        loadTechniques();

        int passes = 0;
        boolean optimizationPerformed;

        do {
            optimizationPerformed = false;

            for (FlowWalker<?, ?> algorithm : algorithms) {
                Optimization<?, ?> optimization = new Optimization<>(algorithm);
                graph = optimization.optimize(graph);
                optimizationPerformed |= optimization.optimizationPerformed();
            }

        } while (optimizationPerformed && passes++ < MAX_OPTIMIZATION_PASSES);

        return graph;
    }

}
