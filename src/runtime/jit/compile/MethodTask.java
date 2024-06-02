package runtime.jit.compile;

import runtime.core.Data;
import runtime.core.TThread;
import runtime.core.VirtualFunction;
import runtime.jit.gen.Generator;
import runtime.jit.graph.FlowNode;
import runtime.jit.graph.build.GraphBuilder;
import runtime.jit.graph.build.GraphBuilder2;
import runtime.jit.optim.Optimizer;
import runtime.jit.optim.OptimizerImpl;
import runtime.jit.table.LookUpTable;
import runtime.type.Callable;

public class MethodTask implements Task {

    private final VirtualFunction function;

    private final Data[] args;

    private final TThread thread;

    public MethodTask(VirtualFunction function, Data[] args, TThread thread){
        this.function = function;
        this.args = args;
        this.thread = thread;
    }

    @Override
    public Kind getKind() {
        return Kind.METHOD;
    }

    @Override
    public void handle(LookUpTable table) {
        if (table.isOptimized(function)) return;
        FlowNode graph = build(function);
        graph = optimize(graph);
        Callable llCode = generate(graph);
        table.addOptimized(function, llCode);
    }

    private FlowNode build(VirtualFunction function) {
        GraphBuilder builder = new GraphBuilder2();
        return builder.build(function.getByteCode());
    }

    private FlowNode optimize(FlowNode graph) {
        Optimizer optimizer = new OptimizerImpl();
        return optimizer.optimize(graph);
    }

    Callable generate(FlowNode graph) {
        Generator generator = null;
        return generator.generate(graph);
    }
}
