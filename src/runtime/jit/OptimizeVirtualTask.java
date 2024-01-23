package runtime.jit;

import runtime.core.Data;
import runtime.core.VirtualFunction;
import runtime.heap.Heap;
import runtime.type.Callable;

public record OptimizeVirtualTask(VirtualFunction called, Data[] args) implements JITTask {

    @Override
    public void handle(JIT jit) {
        if (jit.hasOptimization(called, args)) return;
        BytecodeParser.Tree tree = BytecodeParser.parse(called);
        tree = Optimizer.optimize(tree, args, jit);
        String fileName = called.getName().replaceAll(" ", "_");
        String javaCode = JavaCodeGenerator.generate(fileName, tree, called);
        Callable optimized = Compiler.compile(fileName, javaCode);
        jit.setOptimized(called, optimized, args);
    }

}
