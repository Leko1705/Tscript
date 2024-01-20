package tscriptc.tree;

import tscriptc.util.TreeVisitor;

public interface SuperTree extends ExpressionTree {

    String getName();

    @Override
    default <P, R> R accept(TreeVisitor<P, R> visitor, P p) {
        return visitor.visitSuperTree(this, p);
    }
}
