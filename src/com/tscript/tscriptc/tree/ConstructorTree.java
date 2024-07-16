package com.tscript.tscriptc.tree;

import com.tscript.tscriptc.util.TreeVisitor;

import java.util.List;
import java.util.Set;

public interface ConstructorTree extends Tree {

    Set<Modifier> getModifiers();

    List<ParameterTree> getParameters();

    List<ArgumentTree> getSuperArguments();

    BlockTree getBody();

    @Override
    default <P, R> R accept(TreeVisitor<P, R> visitor, P p) {
        return visitor.visitConstructorTree(this, p);
    }
}
