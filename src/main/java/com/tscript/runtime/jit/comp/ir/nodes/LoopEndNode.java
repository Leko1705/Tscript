package com.tscript.runtime.jit.comp.ir.nodes;

import com.tscript.runtime.jit.comp.ir.Graph;
import com.tscript.runtime.jit.comp.ir.IRControl;
import com.tscript.runtime.jit.comp.ir.IRControlVisitor;
import com.tscript.runtime.jit.comp.ir.IRValue;

import java.util.Collections;
import java.util.List;

public class LoopEndNode extends IRControl {

    private final LoopHeadNode loopHeadCtrl;
    private final List<IRControl> predecessors;

    public LoopEndNode(Graph graph, IRControl body, LoopHeadNode head) {
        super(graph);

        this.predecessors = List.of(body);
        body.getSuccessors().add(this);

        this.loopHeadCtrl = head;
    }

    public LoopHeadNode getTarget() {
        return loopHeadCtrl;
    }

    @Override
    public List<IRControl> getPredecessors() {
        return predecessors;
    }

    @Override
    public List<IRControl> getSuccessors() {
        return Collections.singletonList(loopHeadCtrl);
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
