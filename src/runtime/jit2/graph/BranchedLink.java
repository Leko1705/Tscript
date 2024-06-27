package runtime.jit2.graph;

import runtime.jit2.graph.tree.Tree;

public class BranchedLink implements CFGLink {

    public Tree condition;
    public CFGNode T;
    public CFGNode F;
    public boolean branchIfFalse;
    public boolean isLoop;
    public CFGNode next;

    @Override
    public <R, P> R accept(CFGVisitor<R, P> visitor, P p) {
        return visitor.visitBranchLink(this, p);
    }

}
