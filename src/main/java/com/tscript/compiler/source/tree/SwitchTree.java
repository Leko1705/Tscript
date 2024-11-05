package com.tscript.compiler.source.tree;

import com.tscript.compiler.source.utils.TreeVisitor;

import java.util.List;

public interface SwitchTree extends StatementTree {

    List<? extends CaseTree> getCases();

    StatementTree getDefaultCase();

    @Override
    default <P, R> R accept(TreeVisitor<P, R> visitor, P p) {
        return visitor.visitSwitch(this, p);
    }
}
