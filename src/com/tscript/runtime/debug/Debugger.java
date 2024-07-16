package com.tscript.runtime.debug;

import java.util.Objects;

public abstract class Debugger {

    private static final Debugger VOID_DEBUGGER = new Debugger() {
        @Override
        public DebugAction onDebug(int threadID, VMInfo info) {
            return DebugAction.RESUME;
        }
    };

    public static Debugger getVoidDebugger(){
        return VOID_DEBUGGER;
    }

    public static Debugger getDefaultDebugger(){
        return new ConsoleDebugger();
    }


    private volatile DebugAction action = null;

    public DebugAction onBreakPoint(int threadID, VMInfo vmInfo){

        Thread thread = new Thread(() -> action = Objects.requireNonNull(onDebug(threadID, vmInfo)));
        thread.start();

        try {
            thread.join();
        }catch (Exception ignored){
            action = DebugAction.QUIT;
        }

        return action;
    }

    public abstract DebugAction onDebug(int threadID, VMInfo info);

}
