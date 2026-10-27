package com.tscript.runtime.jit.comp.ir.nodes;

import com.tscript.runtime.jit.comp.ir.*;

import java.util.ArrayList;
import java.util.List;

/**
 * A speculative check. If the check fails, the compiled code is left and the interpreter
 * continues at the state described by the frame state of this guard (deoptimization).
 * <p>
 * Inputs: [check specific inputs..., frame state values..., global write back values...]
 */
public abstract class Guard extends IREffect implements StateCarrier {

    private final FrameState state;
    private final int ownInputs;
    private final List<GlobalRef> globalWrites = new ArrayList<>();

    /** true if this guard checks a loop condition: its failure is the normal way to leave a loop */
    private boolean loopExit = false;

    /**
     * false if no side trace should be attached to this guard (e.g. type speculations on trace
     * entry: if they fail often, the trace should rather be recorded again)
     */
    private boolean sideTraceable = true;

    /** runtime information about this exit, created once when the guard is emitted the first time */
    private Object deoptInfo;

    protected Guard(IRControl control, List<IRValue> ownInputs, FrameState state, List<IRValue> stateValues) {
        super(control, concat(ownInputs, stateValues));
        this.state = state;
        this.ownInputs = ownInputs.size();
        if (stateValues.size() != state.valueCount()) {
            throw new IllegalArgumentException("frame state does not match its values");
        }
    }

    private static List<IRValue> concat(List<IRValue> own, List<IRValue> state) {
        List<IRValue> all = new ArrayList<>(own);
        all.addAll(state);
        return all;
    }

    @Override
    public FrameState getState() {
        return state;
    }

    @Override
    public List<IRValue> getStateValues() {
        return getInputs().subList(ownInputs, ownInputs + state.valueCount());
    }

    @Override
    public List<GlobalRef> getGlobalWrites() {
        return globalWrites;
    }

    @Override
    public List<IRValue> getGlobalWriteValues() {
        int from = ownInputs + state.valueCount();
        return getInputs().subList(from, from + globalWrites.size());
    }

    @Override
    public void addGlobalWrite(GlobalRef global, IRValue value) {
        globalWrites.add(global);
        addInput(value);
    }

    public boolean isSideTraceable() {
        return sideTraceable && state.isRoot();
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

    public Object getDeoptInfo() {
        return deoptInfo;
    }

    public void setDeoptInfo(Object deoptInfo) {
        this.deoptInfo = deoptInfo;
    }
}
