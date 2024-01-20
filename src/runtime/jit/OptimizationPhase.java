package runtime.jit;

public class OptimizationPhase<R, P> extends SimpleTreeVisitor<R, P> {

    private boolean optimizationPerformed = false;

    public OptimizationPhase(){
    }

    public OptimizationPhase(R defaultValue){
        super(defaultValue);
    }

    public BytecodeParser.Tree performOptimization(BytecodeParser.Tree tree){
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
