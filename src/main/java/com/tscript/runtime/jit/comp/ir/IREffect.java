package com.tscript.runtime.jit.comp.ir;

import java.util.List;

public abstract class IREffect implements IRNode {
    public IREffect previous;
    public IREffect next;
    public IRControl control;
    private final Graph graph;

    public IREffect(IREffect previous, IRControl control, Graph graph) {
        this.graph = graph;
        this.previous = previous;
        this.control = control;
    }

    public Graph getGraph() {
        return graph;
    }

    public void remove() {
        IREffect prev =  previous;
        IREffect next = this.next;

        previous.next = next;
        if (next != null) {
            next.previous = prev;
        }
    }

    public void insertBefore(IREffect effect) {
        IREffect prev = previous;

        if (prev != null)
            prev.next = effect;
        effect.previous = prev;

        effect.next = this;
        previous = effect;
    }

    public void insertAfter(IREffect effect) {
        if (next != null) {
            next.insertBefore(effect);
        }
        else {
            next = effect;
            effect.previous = this;
        }
    }

    public void replaceInput(IRValue oldInput, IRValue newInput) {
        newInput.getUsers().add(this);
        oldInput.getUsers().remove(this);
        updateSpecificFields(oldInput, newInput);
    }

    protected void updateSpecificFields(IRValue oldInput, IRValue newInput) {
    }

    public List<IRValue> getInputs() {
        return List.of();
    }

    public abstract <P, R> R accept(IREffectVisitor<P, R> visitor, P p);

    public <P, R> R accept(IREffectVisitor<P, R> visitor) {
        return accept(visitor, null);
    }
}
