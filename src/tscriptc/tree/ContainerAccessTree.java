package tscriptc.tree;

import tscriptc.util.TreeVisitor;

public interface ContainerAccessTree extends UnaryExpressionTree {

    ExpressionTree getExpression();

    ExpressionTree getKey();

    @Override
    default <P, R> R accept(TreeVisitor<P, R> visitor, P p) {
        return visitor.visitContainerAccessTree(this, p);
    }
}
