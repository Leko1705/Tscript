package com.tscript.runtime.jit.ir.nodes;

import com.tscript.runtime.jit.ir.IRNode;
import com.tscript.runtime.jit.ir.types.IRType;

public interface IRValue extends IRNode {

    IRType getType();

    default boolean canBePrimitive() {
        return false;
    }

    default boolean isCallingInstruction() {
        return false;
    }
}
