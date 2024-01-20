package tscriptc.tree;

import tscriptc.util.TreeVisitor;

public interface AssignTree extends BinaryExpressionTree, UsefulExpression {

    @Override
    default <P, R> R accept(TreeVisitor<P, R> visitor, P p) {
        return visitor.visitAssignTree(this, p);
    }
}
