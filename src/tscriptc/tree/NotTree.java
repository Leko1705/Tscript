package tscriptc.tree;

import tscriptc.util.TreeVisitor;

public interface NotTree extends UnaryExpressionTree {

    @Override
    default <P, R> R accept(TreeVisitor<P, R> visitor, P p) {
        return visitor.visitNotTree(this, p);
    }
}
