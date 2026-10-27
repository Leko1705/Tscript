package com.tscript.runtime.jit.comp.ir.nodes;

import com.tscript.runtime.jit.comp.ir.*;

import java.util.ArrayList;
import java.util.List;

public class StartControl extends IRControl {

    private final List<IRControl> successors = new ArrayList<>();

    public StartControl(Graph graph) {
        super(graph);
    }

    @Override
    public List<IRControl> getPredecessors() {
        return List.of();
    }

    @Override
    public List<IRControl> getSuccessors() {
        return successors;
    }

    @Override
    public <P, R> R accept(IRControlVisitor<P, R> visitor, P p) {
        return visitor.visitStart(this, p);
    }
}
