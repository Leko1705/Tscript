package com.tscript.runtime.jit.ir.types;

public interface IRPrimitiveType extends IRType {

    @Override
    default boolean isPrimitive() {
        return true;
    }
}
