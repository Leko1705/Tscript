package com.tscript.runtime.debugger;

public interface DebugActionObserver {

    void notify(Debugger.Action action);
}
