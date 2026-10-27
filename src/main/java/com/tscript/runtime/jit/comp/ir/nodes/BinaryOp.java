package com.tscript.runtime.jit.comp.ir.nodes;

import com.tscript.runtime.jit.comp.ir.IRValue;

import java.util.List;

public abstract class BinaryOp extends IRValue {

    private IRValue lhs;
    private IRValue rhs;

    public BinaryOp(IRValue left, IRValue right) {
        lhs = left;
        rhs = right;

        left.getUsers().add(this);
        right.getUsers().add(this);
    }

    public IRValue getLeft() {
        return lhs;
    }

    public IRValue getRight() {
        return rhs;
    }

    @Override
    public List<IRValue> getInputs() {
        return List.of(getLeft(), getRight());
    }

    @Override
    protected void updateSpecificFields(IRValue oldInput, IRValue newInput) {
        if (oldInput == lhs) {
            lhs = newInput;
        }
        if (oldInput == rhs) {
            rhs = newInput;
        }
    }
}
