package com.tscript.runtime.jit.ir;

public interface IRWalker<P, R> {

    default void walk(IRVisitor<P, R> visitor, IRBlock block) {
        walk(visitor, block, null);
    }

    void walk(IRVisitor<P, R> visitor, IRBlock block, P p);

}
