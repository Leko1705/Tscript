package com.tscript.runtime.debugger;

import com.tscript.runtime.debugger.states.VMState;

public interface Debugger {

    enum Action {
        STEP_OVER,
        STEP_OUT,
        RUN,
        STOP
    }

    void onHalt(VMState state, DebugActionObserver observer);

}
