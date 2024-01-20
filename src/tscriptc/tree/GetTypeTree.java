package tscriptc.tree;

import tscriptc.util.TreeVisitor;

public interface GetTypeTree extends UnaryExpressionTree {

    ExpressionTree getExpression();

    @Override
    default <P, R> R accept(TreeVisitor<P, R> visitor, P p) {
        return visitor.visitGetTypeTree(this, p);
    }
}
