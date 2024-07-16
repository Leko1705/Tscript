package com.tscript.tscriptc.tree;

import com.tscript.tscriptc.util.TreeVisitor;

import java.util.List;

public interface LambdaTree extends ExpressionTree {

    List<ClosureTree> getClosures();

    List<ParameterTree> getParameters();

    BlockTree getBody();

    @Override
    default <P, R> R accept(TreeVisitor<P, R> visitor, P p) {
        return visitor.visitLambdaTree(this, p);
    }
}
