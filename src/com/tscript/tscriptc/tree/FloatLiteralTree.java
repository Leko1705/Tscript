package com.tscript.tscriptc.tree;

import com.tscript.tscriptc.util.TreeVisitor;

import java.nio.ByteBuffer;

public interface FloatLiteralTree extends LiteralTree<Double> {

    Double get();

    @Override
    default <P, R> R accept(TreeVisitor<P, R> visitor, P p) {
        return visitor.visitFloatTree(this, p);
    }
}
