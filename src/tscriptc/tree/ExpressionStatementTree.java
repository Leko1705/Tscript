package tscriptc.tree;

import tscriptc.util.TreeVisitor;

public interface ExpressionStatementTree extends StatementTree {

    ExpressionTree getExpression();

    @Override
    default <P, R> R accept(TreeVisitor<P, R> visitor, P p) {
        return visitor.visitExpressionStatementTree(this, p);
    }
}
