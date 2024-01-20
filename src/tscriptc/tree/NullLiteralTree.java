package tscriptc.tree;

import tscriptc.util.TreeVisitor;

public interface NullLiteralTree extends LiteralTree<Void> {

    default Void get(){
        return null;
    }

    @Override
    default <P, R> R accept(TreeVisitor<P, R> visitor, P p) {
        return visitor.visitNullTree(this, p);
    }

}
