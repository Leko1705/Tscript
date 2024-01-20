package tscriptc.tree;

import tscriptc.util.TreeVisitor;

public interface ThrowTree extends StatementTree {

    ExpressionTree getExpression();

    @Override
    default <P, R> R accept(TreeVisitor<P, R> visitor, P p) {
        return visitor.visitThrowTree(this, p);
    }
}
