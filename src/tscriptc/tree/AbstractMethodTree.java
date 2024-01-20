package tscriptc.tree;

import tscriptc.util.TreeVisitor;

import java.util.List;

public interface AbstractMethodTree extends DefinitionTree {

    @Override
    default <P, R> R accept(TreeVisitor<P, R> visitor, P p) {
        return visitor.visitAbstractMethodTree(this, p);
    }

}
