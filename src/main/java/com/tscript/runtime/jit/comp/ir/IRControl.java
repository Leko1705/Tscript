package com.tscript.runtime.jit.comp.ir;

import java.util.*;

public abstract class IRControl implements IRNode {

    protected final Graph graph;

    private final List<IRValue> inputs;

    protected IRControl(Graph graph, IRValue... inputs) {
        this.graph = graph;
        this.inputs = new ArrayList<>(Arrays.asList(inputs));
        for (IRValue input : this.inputs) {
            input.getUsers().add(this);
        }
    }

    public Graph getGraph() {
        return graph;
    }

    protected IRValue input(int index) {
        return inputs.get(index);
    }

    protected void addInput(IRValue value) {
        inputs.add(value);
        value.getUsers().add(this);
    }

    public abstract List<IRControl> getPredecessors();

    public abstract List<IRControl> getSuccessors();

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

    public abstract <P, R> R accept(IRControlVisitor<P, R> visitor, P p);

    public <P, R> R accept(IRControlVisitor<P, R> visitor) {
        return accept(visitor, null);
    }

}
