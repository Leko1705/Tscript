package com.tscript.runtime.jit.comp.ir.nodes;

import com.tscript.runtime.jit.comp.ir.*;

import java.util.ArrayList;
import java.util.List;

/**
 * A speculative check. If the check fails, the compiled code is left and the interpreter
 * continues at the state described by the frame state of this guard (deoptimization).
 * <p>
 * Inputs: [check specific inputs..., locals..., stack...]
 */
public abstract class Guard extends IREffect {

    private final FrameState state;
    private final int ownInputs;

    /**
     * true if this guard checks a loop condition: its failure is the normal way to leave a loop
     */
    private boolean loopExit = false;

    protected Guard(IRControl control, List<IRValue> ownInputs, FrameState state, List<IRValue> locals, List<IRValue> stack) {
        super(control, concat(ownInputs, locals, stack));
        this.state = state;
        this.ownInputs = ownInputs.size();
        if (locals.size() != state.localIndices().length || stack.size() != state.stackSize()) {
            throw new IllegalArgumentException("frame state does not match its values");
        }
    }

    private static List<IRValue> concat(List<IRValue> own, List<IRValue> locals, List<IRValue> stack) {
        List<IRValue> all = new ArrayList<>(own);
        all.addAll(locals);
        all.addAll(stack);
        return all;
    }

    public FrameState getState() {
        return state;
    }

    public List<IRValue> getLocals() {
        return getInputs().subList(ownInputs, ownInputs + state.localIndices().length);
    }

    public List<IRValue> getStack() {
        int from = ownInputs + state.localIndices().length;
        return getInputs().subList(from, from + state.stackSize());
    }

    /**
     * false if no side trace should be attached to this guard (e.g. type speculations on trace
     * entry: if they fail often, the trace should rather be recorded again)
     */
    private boolean sideTraceable = true;

    public boolean isSideTraceable() {
        return sideTraceable;
    }

    public void setSideTraceable(boolean sideTraceable) {
        this.sideTraceable = sideTraceable;
    }

    public boolean isLoopExit() {
        return loopExit;
    }

    public void setLoopExit(boolean loopExit) {
        this.loopExit = loopExit;
    }
}
