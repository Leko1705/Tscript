package tscriptc.tree;

import tscriptc.util.TreeVisitor;

public interface NativeFunctionTree extends DefinitionTree {

    @Override
    default <P, R> R accept(TreeVisitor<P, R> visitor, P p) {
        return visitor.visitNativeFunctionTree(this, p);
    }
}
