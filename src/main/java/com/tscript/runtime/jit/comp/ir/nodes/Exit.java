package com.tscript.runtime.jit.comp.ir.nodes;

import com.tscript.runtime.jit.comp.ir.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Leaves compiled code unconditionally and continues in the interpreter at the given state.
 * Used by side traces that end by jumping back to a loop header that is not part of the trace.
 * <p>
 * Inputs: [locals..., stack...]
 */
public class Exit extends IRControl {

    private final IRControl previous;
    private final FrameState state;

    public Exit(FrameState state, List<IRValue> locals, List<IRValue> stack, IRControl control, Graph graph) {
        super(graph, concat(locals, stack).toArray(new IRValue[0]));
        this.previous = control;
        this.state = state;
        control.getSuccessors().add(this);
    }

    private static List<IRValue> concat(List<IRValue> locals, List<IRValue> stack) {
        List<IRValue> all = new ArrayList<>(locals);
        all.addAll(stack);
        return all;
    }

    public FrameState getState() {
        return state;
    }

    public List<IRValue> getLocals() {
        return getInputs().subList(0, state.localIndices().length);
    }

    public List<IRValue> getStack() {
        return getInputs().subList(state.localIndices().length, getInputs().size());
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
        return visitor.visitExit(this, p);
    }
}
