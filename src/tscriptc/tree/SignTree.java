package tscriptc.tree;


import tscriptc.util.TreeVisitor;

public interface SignTree extends UnaryExpressionTree {

    boolean isNegation();

    @Override
    default <P, R> R accept(TreeVisitor<P, R> visitor, P p) {
        return visitor.visitSignTree(this, p);
    }
}
