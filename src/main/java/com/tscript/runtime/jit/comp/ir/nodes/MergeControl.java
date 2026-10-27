package com.tscript.runtime.jit.comp.ir.nodes;

import com.tscript.runtime.jit.comp.ir.Graph;
import com.tscript.runtime.jit.comp.ir.IRControl;
import com.tscript.runtime.jit.comp.ir.IRControlVisitor;
import com.tscript.runtime.jit.comp.ir.IRValue;

import java.util.ArrayList;
import java.util.List;

public class MergeControl extends IRControl {

    private final List<IRControl> controls;
    private final List<IRControl> successors;

    public MergeControl(Graph graph, List<IRControl> controls) {
        super(graph);
        this.controls = controls;
        this.successors = new ArrayList<>();
        for (IRControl control : controls) {
            control.getSuccessors().add(this);
        }
    }

    public MergeControl(Graph graph, IRControl... controls) {
        super(graph);
        this.controls = List.of(controls);
        this.successors = new ArrayList<>();
        for (IRControl control : controls) {
            control.getSuccessors().add(this);
        }
    }

    @Override
    public List<IRControl> getPredecessors() {
        return controls;
    }

    @Override
    public List<IRControl> getSuccessors() {
        return successors;
    }

    @Override
    public List<IRValue> getInputs() {
        return List.of();
    }

    @Override
    public <P, R> R accept(IRControlVisitor<P, R> visitor, P p) {
        return visitor.visitMerge(this, p);
    }

}
