package runtime.jit;

import runtime.core.Data;

public class OptimizationPhase<R, P> extends SimpleTreeVisitor<R, P> {

    private boolean optimizationPerformed = false;

    protected Data[] args;

    protected JIT jit;

    public OptimizationPhase(){
    }

    public OptimizationPhase(R defaultValue){
        super(defaultValue);
    }

    public BytecodeParser.Tree performOptimization(BytecodeParser.Tree tree, Data[] args, JIT jit){
        this.args = args;
        this.jit = jit;
        tree.accept(this);
        return tree;
    }

    public void reset(){
        optimizationPerformed = false;
    }

    public void optimizationPerformed(){
        optimizationPerformed = true;
    }

    public boolean isOptimizationPerformed() {
        return optimizationPerformed;
    }

    @Override
    public R visitSequenceTree(BytecodeParser.SequenceTree sequenceTree, P p) {
        R r = getDefaultValue();
        for (int i = 0; i < sequenceTree.children.size(); i++){
            BytecodeParser.Tree child = sequenceTree.children.get(i);
            r = scanSelective(child, p, r);
        }
        return r;
    }
}
