package tscriptc.tree;

@InheritOnly
public interface BinaryExpressionTree extends ExpressionTree {

    ExpressionTree getLeft();

    ExpressionTree getRight();

}
