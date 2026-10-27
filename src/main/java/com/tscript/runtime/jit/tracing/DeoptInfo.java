package com.tscript.runtime.jit.tracing;

import com.tscript.runtime.jit.comp.ir.FrameState;

/**
 * Static information about a point where compiled code is left (a failing guard or a
 * trace exit). Instances are compared by identity: every exit point has its own info, which is
 * also used as key for the side trace attached to that exit.
 */
public final class DeoptInfo {

    private final FrameState state;
    private final boolean loopExit;
    private final boolean jumpExit;
    private final boolean sideTraceable;

    /** cache for the image cache entry of the side trace attached to this exit */
    volatile Object sideEntry = null;

    /**
     * @param state         the interpreter state to restore
     * @param loopExit      true if this is the regular exit of a compiled loop (not a misspeculation)
     * @param jumpExit      true if the trace ended here on purpose to continue at a loop header
     * @param sideTraceable true if a side trace may be recorded from this exit when it gets hot
     */
    public DeoptInfo(FrameState state, boolean loopExit, boolean jumpExit, boolean sideTraceable) {
        this.state = state;
        this.loopExit = loopExit;
        this.jumpExit = jumpExit;
        this.sideTraceable = sideTraceable;
    }

    public FrameState state() {
        return state;
    }

    public boolean loopExit() {
        return loopExit;
    }

    public boolean jumpExit() {
        return jumpExit;
    }

    public boolean sideTraceable() {
        return sideTraceable;
    }

    @Override
    public String toString() {
        return "exit at ip " + state.ip() + (loopExit ? " (loop exit)" : "") + (jumpExit ? " (jump)" : "");
    }
}
