package runtime.jit;

import runtime.core.VirtualFunction;
import runtime.type.Callable;

public record OptimizeVirtualTask(VirtualFunction called) implements JITTask {

    @Override
    public synchronized void handle(JIT jit) {
        if (jit.hasOptimization(called.getName())) return;
        BytecodeParser.Tree tree = BytecodeParser.parse(called);
        tree = Optimizer.optimize(tree);
        String javaCode = JavaCodeGenerator.generate(called.getName(), tree, called);
        Callable optimized = Compiler.compile(called.getName(), javaCode);
        jit.setOptimized(called.getName(), optimized);
    }

}
