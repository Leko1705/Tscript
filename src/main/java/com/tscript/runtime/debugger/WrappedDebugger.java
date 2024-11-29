package com.tscript.runtime.debugger;

import com.tscript.runtime.debugger.states.VMState;

public abstract class WrappedDebugger implements Debugger {

    private DebugActionObserver observer;

    @Override
    public void onHalt(VMState state, DebugActionObserver observer) {
        this.observer = observer;
        onHalt(state);
    }

    public abstract void onHalt(VMState state);

    public void notify(Debugger.Action action){
        observer.onAction(action);
    }

}
