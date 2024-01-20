package runtime.jit;

import java.util.List;

public class Optimizer {

    private static final List<OptimizationPhase<?, ?>> phases =
            List.of(
                    new LoopUnoller(),
                    new ConstantFolder(), new DeadVariableEliminator(),
                    new DeadBlockEliminator(), new UnsafeReducer());

    public static BytecodeParser.Tree optimize(BytecodeParser.Tree tree){
        boolean optimizationPerformed;
        do {
            optimizationPerformed = false;
            for (OptimizationPhase<?, ?> phase : phases){
                phase.reset();
                tree = phase.performOptimization(tree);
                optimizationPerformed = optimizationPerformed || phase.isOptimizationPerformed();
            }
        }while (optimizationPerformed);
        return tree;
    }

}
