package com.tscript.tscriptc.tree;

import com.tscript.tscriptc.util.TreeVisitor;

import java.util.List;

public interface ClassTree extends DefinitionTree {

    String getName();

    String getSuper();

    ConstructorTree getConstructor();

    List<DefinitionTree> getDefinitions();

    @Override
    default <P, R> R accept(TreeVisitor<P, R> visitor, P p) {
        return visitor.visitClassTree(this, p);
    }
}
