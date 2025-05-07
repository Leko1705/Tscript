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

    default boolean isType(Class<? extends IRType> type) {
        return accept(new IsIRType(), type);
    }

    default boolean isNumeric() {
        return isType(IRIntegerType.class) || isType(IRRealType.class);
    }

}
