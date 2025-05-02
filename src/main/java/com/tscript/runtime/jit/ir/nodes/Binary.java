package com.tscript.runtime.jit.ir.nodes;

import com.tscript.runtime.jit.ir.IRNode;

public abstract class Binary implements IRNode {

    public IRNode lhs;
    public IRNode rhs;

    public Binary(IRNode lhs, IRNode rhs) {
        this.lhs = lhs;
        this.rhs = rhs;
    }

}
