package com.tscript.runtime.jit.comp.ir;

import java.util.ArrayList;
import java.util.List;

public abstract class IRControl implements IRNode {

    private final List<IRControl> predecessors;

    private final List<IRControl> successors;

    protected final Graph graph;

    public IRControl(Graph graph) {
        this.graph = graph;
        this.predecessors = new ArrayList<>();
        this.successors = new ArrayList<>();
    }
    public Graph getGraph() {
        return graph;
    }

    public List<IRControl> getPredecessors() {
        return predecessors;
    }

    public List<IRControl> getSuccessors() {
        return successors;
    }

    public void replaceInput(IRValue oldInput, IRValue newInput) {
        newInput.getUsers().add(this);
        oldInput.getUsers().remove(this);
        updateSpecificFields(oldInput, newInput);
    }

    protected void updateSpecificFields(IRValue oldInput, IRValue newInput) {
    }

    public abstract List<IRValue> getInputs();

    public abstract <P, R> R accept(IRControlVisitor<P, R> visitor, P p);

    public  <P, R> R accept(IRControlVisitor<P, R> visitor) {
        return accept(visitor, null);
    }

}
