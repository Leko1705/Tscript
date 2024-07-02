package runtime.jit2.graph.creation;

import runtime.jit2.graph.CFGNode;
import runtime.jit2.graph.tree.Tree;

public class CFGBuilder2 {

    private interface CFGComponent {
        <P, R> R accept(ComponentVisitor<P, R> visitor, P p);
    }


    private static class StatementComponent implements CFGComponent {
        public final Tree tree;
        private StatementComponent(Tree tree) {
            this.tree = tree;
        }
        @Override
        public <P, R> R accept(ComponentVisitor<P, R> visitor, P p) {
            return null;
        }
    }

    public static class BranchComponent implements CFGComponent {

        @Override
        public <P, R> R accept(ComponentVisitor<P, R> visitor, P p) {
            return null;
        }
    }

    public static class JumpComponent implements CFGComponent {
        @Override
        public <P, R> R accept(ComponentVisitor<P, R> visitor, P p) {
            return null;
        }
    }

    private interface ComponentVisitor<P, R> {
        R visitStatement(StatementComponent comp, P p);
        R visitBranch(BranchComponent comp, P p);
    }






}
