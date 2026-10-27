package com.tscript.runtime.jit.comp.ir.nodes;

import com.tscript.runtime.jit.comp.ir.*;

import java.util.List;

/**
 * Continues if the condition (a boolean value) is true, deoptimizes otherwise.
 */
public class GuardBool extends Guard {

    public GuardBool(IRValue condition, FrameState state, List<IRValue> stateValues, IRControl control) {
        super(control, List.of(condition), state, stateValues);
    }

    /**
     * A speculative guard only exists to establish the type of a value (the narrowed Pi node).
     * If that Pi is unused, the guard may be removed.
     */
    private Pi narrowed = null;

    public Pi getNarrowed() {
        return narrowed;
    }

    public void setNarrowed(Pi narrowed) {
        this.narrowed = narrowed;
    }

    public IRValue getCondition() {
        return input(0);
    }

    @Override
    public <P, R> R accept(IREffectVisitor<P, R> visitor, P p) {
        return visitor.visitGuardBool(this, p);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("GuardBool(#" + getCondition().id + ", " + getState());
        sb.append(", values=[");
        for (IRValue v : getStateValues()) sb.append(" #").append(v.id);
        sb.append(" ]");
        if (!getGlobalWrites().isEmpty()) sb.append(", writes=").append(getGlobalWrites());
        if (isLoopExit()) sb.append(", loop-exit");
        return sb.append(")").toString();
    }
}
