package com.tscript.runtime.jit.comp.ir.nodes;

import com.tscript.runtime.jit.comp.ir.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Control flow merge. Not produced by the tracer; intended for a future method JIT front end.
 */
public class MergeControl extends IRControl {

    private final List<IRControl> controls;
    private final List<IRControl> successors = new ArrayList<>();

    public MergeControl(Graph graph, IRControl... controls) {
        super(graph);
        this.controls = List.of(controls);
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
    public <P, R> R accept(IRControlVisitor<P, R> visitor, P p) {
        return visitor.visitMerge(this, p);
    }
}
