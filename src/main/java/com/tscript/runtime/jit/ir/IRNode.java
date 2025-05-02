package com.tscript.runtime.jit.ir;

public interface IRNode {

    <P, R> R accept(IRVisitor<P, R> visitor, P p);

    default <P, R> R accept(IRVisitor<P, R> visitor) {
        return accept(visitor, null);
    }

}
