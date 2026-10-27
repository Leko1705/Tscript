package com.tscript.runtime.jit.comp.ir;

import java.util.*;

/**
 * A node of the (linear) effect chain. Effects are executed in chain order.
 */
public abstract class IREffect implements IRNode {

    public IREffect previous;
    public IREffect next;
    public IRControl control;

    private final List<IRValue> inputs;

    protected IREffect(IRControl control, IRValue... inputs) {
        this(control, Arrays.asList(inputs));
    }

    protected IREffect(IRControl control, List<IRValue> inputs) {
        this.control = control;
        this.inputs = new ArrayList<>(inputs);
        for (IRValue input : this.inputs) {
            input.getUsers().add(this);
        }
    }

    protected IRValue input(int index) {
        return inputs.get(index);
    }

    protected void addInput(IRValue value) {
        inputs.add(value);
        value.getUsers().add(this);
    }

    @Override
    public List<IRValue> getInputs() {
        return Collections.unmodifiableList(inputs);
    }

    @Override
    public void replaceInput(IRValue oldInput, IRValue newInput) {
        boolean replaced = false;
        for (int i = 0; i < inputs.size(); i++) {
            if (inputs.get(i) == oldInput) {
                inputs.set(i, newInput);
                replaced = true;
            }
        }
        if (replaced) {
            oldInput.getUsers().remove(this);
            newInput.getUsers().add(this);
        }
    }

    /**
     * Unlinks this effect from the chain and from its inputs.
     */
    public void remove() {
        if (previous != null) previous.next = next;
        if (next != null) next.previous = previous;
        previous = null;
        next = null;
        for (IRValue input : inputs) {
            input.getUsers().remove(this);
        }
    }

    public void insertAfter(IREffect effect) {
        effect.previous = this;
        effect.next = next;
        if (next != null) next.previous = effect;
        next = effect;
    }

    public abstract <P, R> R accept(IREffectVisitor<P, R> visitor, P p);

    public <P, R> R accept(IREffectVisitor<P, R> visitor) {
        return accept(visitor, null);
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner(", ", getClass().getSimpleName() + "(", ")");
        for (IRValue input : inputs) sj.add("#" + input.id);
        return sj.toString();
    }
}
