package tscriptc.tree;

import tscriptc.util.TreeVisitor;

public interface ImportTree extends StatementTree {

    String getName();

    @Override
    default <P, R> R accept(TreeVisitor<P, R> visitor, P p) {
        return visitor.visitImportTree(this, p);
    }
}
