package com.tscript.runtime.jit.ir.nodes;

public abstract class Binary implements IRValue {

    public IRValue lhs;
    public IRValue rhs;

    public Binary(IRValue lhs, IRValue rhs) {
        this.lhs = lhs;
        this.rhs = rhs;
    }

    @Override
    public boolean replace(IRValue oldNode, IRValue newNode) {
        if (oldNode == rhs) {
            rhs = newNode;
            return true;
        }
        if (oldNode == lhs) {
            lhs = newNode;
            return true;
        }
        return false;
    }
}
