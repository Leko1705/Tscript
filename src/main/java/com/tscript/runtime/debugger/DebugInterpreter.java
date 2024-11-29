package com.tscript.runtime.debugger;

import com.tscript.runtime.core.Interpreter;
import com.tscript.runtime.core.InterpreterDecorator;
import com.tscript.runtime.core.TThread;

import java.util.*;

public class DebugInterpreter extends InterpreterDecorator implements DebugActionObserver {

    private static final Object lock = new Object();


    private final Debugger debugger;

    private final Set<Integer> breakpoints;

    private Debugger.Action action = Debugger.Action.RUN;


    public DebugInterpreter(Interpreter interpreter, Debugger debugger) {
        this(interpreter, debugger, new HashSet<>());
    }

    public DebugInterpreter(Interpreter interpreter, Debugger debugger, Set<Integer> breakpoints) {
        super(interpreter);
        this.debugger = Objects.requireNonNull(debugger);
        this.breakpoints = Objects.requireNonNull(breakpoints);
    }

    public void addBreakpoint(int line) {
        breakpoints.add(line);
    }

    public void removeBreakpoint(int line) {
        breakpoints.remove(line);
    }

    @Override
    public void newLine(int line) {
        super.newLine(line);
        if (breakpoints.contains(line) || action == Debugger.Action.STEP_OVER) {
            halt();
        }
    }

    @Override
    public void callInplace(byte argCount) {
        super.callInplace(argCount);
        if (action == Debugger.Action.STEP_OUT || action == Debugger.Action.STEP_OVER) {
            halt();
        }
    }

    @Override
    public void callMapped(byte argCount) {
        super.callMapped(argCount);
        if (action == Debugger.Action.STEP_OUT || action == Debugger.Action.STEP_OVER) {
            halt();
        }
    }

    @Override
    public void callSuper(byte argCount) {
        super.callSuper(argCount);
        if (action == Debugger.Action.STEP_OUT || action == Debugger.Action.STEP_OVER) {
            halt();
        }
    }

    private void halt() {
        synchronized (lock) {
            getCurrentThread().checkHalt();

            Collection<TThread> threads = getCurrentThread().getVM().getThreads();
            for (TThread thread : threads) {
                if (thread != getCurrentThread()) {
                    thread.halt();
                }
            }

            Thread debugThread = new Thread(() -> debugger.onHalt(this));
            debugThread.start();

            getCurrentThread().halt();
        }
    }


    @Override
    public void onAction(Debugger.Action action) {
        this.action = action;
        Collection<TThread> threads = getCurrentThread().getVM().getThreads();
        for (TThread thread : threads) {
            thread.release();
        }
        if (action == Debugger.Action.STOP){
            getCurrentThread().getVM().exit(0);
        }
    }
}
