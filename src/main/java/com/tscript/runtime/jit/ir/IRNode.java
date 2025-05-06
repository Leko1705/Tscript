package com.tscript.runtime.jit.ir;

import com.tscript.runtime.jit.ir.nodes.IRValue;

public interface IRNode {

    <P, R> R accept(IRVisitor<P, R> visitor, P p);

    default <P, R> R accept(IRVisitor<P, R> visitor) {
        return accept(visitor, null);
    }

    default boolean replace(IRValue oldNode, IRValue newNode) {
        return false;
    }

}
