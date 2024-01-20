package tscriptc.tree;

import tscriptc.util.TreeVisitor;

public interface ContinueTree extends StatementTree {

    @Override
    default <P, R> R accept(TreeVisitor<P, R> visitor, P p) {
        return visitor.visitContinueTree(this, p);
    }
}
