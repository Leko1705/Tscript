package com.tscript.runtime.jit.comp.ir.nodes;

import com.tscript.runtime.jit.comp.ir.*;

import java.util.List;

public class ValueEffect extends IREffect {

    private IRValue value;

    public ValueEffect(IRValue value, IREffect previous, IRControl control, Graph graph) {
        super(previous, control,  graph);
        this.value = value;
        value.getUsers().add(this);
    }

    public IRValue getValue() {
        return value;
    }

    @Override
    protected void updateSpecificFields(IRValue oldInput, IRValue newInput) {
        if (value == oldInput) {
            value = newInput;
        }
    }

    @Override
    public List<IRValue> getInputs() {
        return List.of(value);
    }

    @Override
    public <P, R> R accept(IREffectVisitor<P, R> visitor, P p) {
        return visitor.visitValueEffect(this, p);
    }

    @Override
    public String toString() {
        return "ValueEffect [value=" + getValue() + "]";
    }
}
