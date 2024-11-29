package com.tscript.runtime.debugger;

import com.tscript.runtime.debugger.states.VMState;

public interface Debugger {

    enum Action {
        STEP_OVER,
        STEP_OUT,
        RESUME,
        QUIT
    }

    void onHalt(long threadId, VMState state, DebugActionObserver observer);

}
