package com.tscript.runtime.debugger;

import com.tscript.runtime.debugger.states.VMState;

public abstract class WrappedDebugger implements Debugger, DebugActionObserver {

    private DebugActionObserver observer;

    @Override
    public void onHalt(long threadId, VMState state, int line, DebugActionObserver observer) {
        this.observer = observer;
        onHalt(threadId, state, line);
    }

    public abstract void onHalt(long threadId, VMState state, int line);

    public void notify(Action action){
        observer.notify(action);
    }

}
