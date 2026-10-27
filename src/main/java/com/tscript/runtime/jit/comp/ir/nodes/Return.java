package com.tscript.runtime.jit.comp.ir.nodes;

import com.tscript.runtime.jit.comp.ir.*;

import java.util.List;

public class Return extends IRControl {

    private final IRControl previous;

    public Return(IRValue value, IRControl control, Graph graph) {
        super(graph, value);
        this.previous = control;
        control.getSuccessors().add(this);
    }

    public IRValue getValue() {
        return input(0);
    }

    @Override
    public List<IRControl> getPredecessors() {
        return List.of(previous);
    }

    @Override
    public List<IRControl> getSuccessors() {
        return List.of();
    }

    @Override
    public <P, R> R accept(IRControlVisitor<P, R> visitor, P p) {
        return visitor.visitReturn(this, p);
    }
}
