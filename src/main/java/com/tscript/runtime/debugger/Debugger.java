package com.tscript.runtime.debugger;

public interface Debugger {

    enum Action {
        STEP_OVER,
        STEP_OUT,
        RUN,
        STOP
    }

    void onHalt(DebugActionObserver observer);

}
