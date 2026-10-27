package com.tscript.runtime.jit.comp.ir.nodes;

import com.tscript.runtime.jit.comp.ir.*;

import java.util.List;

/**
 * The back edge of a loop: jumps back to the loop header.
 */
public class LoopEndNode extends IRControl {

    private final IRControl body;
    private final LoopHeadNode loopHead;

    public LoopEndNode(Graph graph, IRControl body, LoopHeadNode head) {
        super(graph);
        this.body = body;
        this.loopHead = head;
        body.getSuccessors().add(this);
        head.setLoopEnd(this);
    }

    public LoopHeadNode getTarget() {
        return loopHead;
    }

    @Override
    public List<IRControl> getPredecessors() {
        return List.of(body);
    }

    @Override
    public List<IRControl> getSuccessors() {
        return List.of(loopHead);
    }

    @Override
    public <P, R> R accept(IRControlVisitor<P, R> visitor, P p) {
        return visitor.visitLoopEnd(this, p);
    }
}
