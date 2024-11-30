package com.tscript.runtime.debugger;

import com.tscript.runtime.debugger.states.VMState;

public abstract class WrappedDebugger implements Debugger, DebugActionObserver {

    private DebugActionObserver observer;

    @Override
    public void onHalt(long threadId, VMState state, DebugActionObserver observer) {
        this.observer = observer;
        onHalt(threadId, state);
    }

    public abstract void onHalt(long threadId, VMState state);

    public void notify(Debugger.Action action){
        observer.notify(action);
    }

}
