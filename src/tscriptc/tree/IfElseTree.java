package tscriptc.tree;

import tscriptc.util.TreeVisitor;

public interface IfElseTree extends StatementTree {

    ExpressionTree getCondition();

    StatementTree getIfBody();

    StatementTree getElseBody();

    @Override
    default <P, R> R accept(TreeVisitor<P, R> visitor, P p) {
        return visitor.visitIfElseTree(this, p);
    }
}
