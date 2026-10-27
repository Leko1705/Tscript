package com.tscript.runtime.jit.comp.ir.nodes;

import com.tscript.runtime.jit.comp.ir.*;

import java.util.List;

public class PushFrame extends IREffect {

    private IRValue called;

    public PushFrame(IRValue called, IREffect effect, IRControl control, Graph graph) {
        super(effect, control, graph);
        this.called = called;
    }

    @Override
    public <P, R> R accept(IREffectVisitor<P, R> visitor, P p) {
        return visitor.visitPushFrame(this, p);
    }

    public IRValue getCalled() {
        return called;
    }

    @Override
    public String toString() {
        return "pushFrame " + getCalled().toString();
    }

    @Override
    protected void updateSpecificFields(IRValue oldInput, IRValue newInput) {
        if (oldInput == called) {
            called = newInput;
        }
    }

    @Override
    public List<IRValue> getInputs() {
        return List.of(called);
    }
}
