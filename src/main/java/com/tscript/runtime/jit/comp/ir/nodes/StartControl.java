package com.tscript.runtime.jit.comp.ir.nodes;

import com.tscript.runtime.jit.comp.ir.Graph;
import com.tscript.runtime.jit.comp.ir.IRControl;
import com.tscript.runtime.jit.comp.ir.IRControlVisitor;
import com.tscript.runtime.jit.comp.ir.IRValue;

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
    public List<IRValue> getInputs() {
        return List.of();
    }

    @Override
    public <P, R> R accept(IRControlVisitor<P, R> visitor, P p) {
        return visitor.visitStart(this, p);
    }

}
