package com.tscript.runtime.jit.comp.ir.nodes;

import com.tscript.runtime.jit.comp.ir.Graph;
import com.tscript.runtime.jit.comp.ir.IRControl;
import com.tscript.runtime.jit.comp.ir.IRControlVisitor;
import com.tscript.runtime.jit.comp.ir.IRValue;

import java.util.ArrayList;
import java.util.List;

public class LoopHeadNode extends IRControl {

    private final List<IRControl> backedges;
    private final IRControl preHeaderCtrl;
    private final List<IRControl> successors;

    public LoopHeadNode(Graph graph, IRControl preHeaderCtrl) {
        super(graph);

        preHeaderCtrl.getSuccessors().add(this);

        backedges = new ArrayList<>();
        this.preHeaderCtrl = preHeaderCtrl;
        successors = new ArrayList<>();
        backedges.add(preHeaderCtrl);
    }



    public void addBackedge(LoopEndNode backEdgeCtrl) {
        backedges.add(backEdgeCtrl);
    }

    public List<IRControl> getBackedges() {
        return backedges;
    }

    public IRControl getPreHeaderCtrl() {
        return preHeaderCtrl;
    }

    @Override
    public List<IRControl> getPredecessors() {
        List<IRControl> predecessors = new ArrayList<>(backedges);
        predecessors.add(preHeaderCtrl);
        return predecessors;
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
        return null;
    }
}
