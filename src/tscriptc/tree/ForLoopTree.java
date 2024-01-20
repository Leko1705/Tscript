package tscriptc.tree;

import tscriptc.util.TreeVisitor;

public interface ForLoopTree extends StatementTree {

    boolean isDeclaration();

    String getName();

    ExpressionTree getIterable();

    StatementTree getBody();

    @Override
    default <P, R> R accept(TreeVisitor<P, R> visitor, P p) {
        return visitor.visitForLoopTree(this, p);
    }
}
