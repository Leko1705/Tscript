package tscriptc.tree;

import tscriptc.util.TreeVisitor;

public interface ThisTree extends ExpressionTree {

    @Override
    default <P, R> R accept(TreeVisitor<P, R> visitor, P p) {
        return visitor.visitThisTree(this, p);
    }
}
