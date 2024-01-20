package tscriptc.tree;

import tscriptc.util.TreeVisitor;

import java.nio.charset.StandardCharsets;

public interface StringLiteralTree extends LiteralTree<String> {

    String get();

    @Override
    default <P, R> R accept(TreeVisitor<P, R> visitor, P p) {
        return visitor.visitStringTree(this, p);
    }

}
