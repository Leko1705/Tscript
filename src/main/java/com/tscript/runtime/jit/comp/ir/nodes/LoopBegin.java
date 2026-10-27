package com.tscript.runtime.jit.comp.ir.nodes;

import com.tscript.runtime.jit.comp.ir.*;

/**
 * Marks the position of a loop header in the effect chain. Everything after this effect
 * (up to the {@link LoopEndNode}) is part of the loop body.
 */
public class LoopBegin extends IREffect {

    public LoopBegin(LoopHeadNode loop) {
        super(loop);
    }

    public LoopHeadNode getLoop() {
        return (LoopHeadNode) control;
    }

    @Override
    public <P, R> R accept(IREffectVisitor<P, R> visitor, P p) {
        return visitor.visitLoopBegin(this, p);
    }
}
