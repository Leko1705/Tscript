package com.tscript.runtime.jit.comp.ir.nodes;

import com.tscript.runtime.jit.comp.ir.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Leaves compiled code unconditionally and continues in the interpreter at the given state.
 * Used by side traces that end by jumping back to a loop header that is not part of the trace.
 * <p>
 * Inputs: [frame state values..., global write back values and virtual objects (in the order they
 * were added)...]
 */
public class Exit extends IRControl implements StateCarrier {

    private final IRControl previous;
    private final FrameState state;
    private final List<GlobalRef> globalWrites = new ArrayList<>();
    private final List<Integer> globalWriteInputs = new ArrayList<>();
    private final List<VirtualObjectState> virtualObjects = new ArrayList<>();
    private Object deoptInfo;

    public Exit(FrameState state, List<IRValue> stateValues, IRControl control, Graph graph) {
        super(graph, stateValues.toArray(new IRValue[0]));
        this.previous = control;
        this.state = state;
        control.getSuccessors().add(this);
    }

    @Override
    public FrameState getState() {
        return state;
    }

    @Override
    public List<IRValue> getStateValues() {
        return getInputs().subList(0, state.valueCount());
    }

    @Override
    public List<GlobalRef> getGlobalWrites() {
        return globalWrites;
    }

    @Override
    public List<IRValue> getGlobalWriteValues() {
        List<IRValue> values = new ArrayList<>(globalWriteInputs.size());
        for (int input : globalWriteInputs) values.add(getInputs().get(input));
        return values;
    }

    @Override
    public void addGlobalWrite(GlobalRef global, IRValue value) {
        globalWrites.add(global);
        globalWriteInputs.add(getInputs().size());
        addInput(value);
    }

    @Override
    public List<VirtualObjectState> getVirtualObjects() {
        return virtualObjects;
    }

    @Override
    public void addVirtualObject(NewObject object, int[] members, List<IRValue> values) {
        virtualObjects.add(new VirtualObjectState(object, members, getInputs().size()));
        addInput(object);
        for (IRValue value : values) addInput(value);
    }

    public Object getDeoptInfo() {
        return deoptInfo;
    }

    public void setDeoptInfo(Object deoptInfo) {
        this.deoptInfo = deoptInfo;
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
