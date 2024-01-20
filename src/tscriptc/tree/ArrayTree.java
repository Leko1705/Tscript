package tscriptc.tree;

import tscriptc.util.TreeVisitor;

import java.util.List;

public interface ArrayTree extends ExpressionTree {

    List<ExpressionTree> getContent();

    @Override
    default <P, R> R accept(TreeVisitor<P, R> visitor, P p) {
        return visitor.visitArrayTree(this, p);
    }
}
