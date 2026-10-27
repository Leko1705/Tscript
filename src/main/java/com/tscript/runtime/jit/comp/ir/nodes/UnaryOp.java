package com.tscript.runtime.jit.comp.ir.nodes;

import com.tscript.runtime.jit.comp.ir.IRNode;
import com.tscript.runtime.jit.comp.ir.IRValue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class UnaryOp extends IRValue {
    private IRValue value;

    private final Set<IRNode> users;

    private final List<IRValue> inputs;

    public UnaryOp(IRValue value) {
        this.value = value;
        this.inputs = new ArrayList<>(List.of(value));
        this.users = new HashSet<>();
        value.getUsers().add(this);
    }

    public IRValue getValue() {
        return value;
    }

    @Override
    public List<IRValue> getInputs() {
        return inputs;
    }

    @Override
    public Set<IRNode> getUsers() {
        return users;
    }

    @Override
    protected void updateSpecificFields(IRValue oldInput, IRValue newInput) {
        if (value == oldInput) {
            value = newInput;
        }
    }
}
