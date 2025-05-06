package com.tscript.runtime.jit.ir.types;

public interface IRType {

    default boolean isPrimitive() {
        return false;
    }

    default boolean isCallable() {
        return false;
    }

    default boolean isIterable() {
        return false;
    }

    <P, R> R accept(IRTypeVisitor<P, R> visitor, P p);

}
