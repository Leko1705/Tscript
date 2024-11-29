package com.tscript.runtime.debugger;

import com.tscript.runtime.core.*;
import com.tscript.runtime.debugger.states.FrameState;
import com.tscript.runtime.debugger.states.ObjectState;
import com.tscript.runtime.debugger.states.ThreadState;
import com.tscript.runtime.debugger.states.VMState;
import com.tscript.runtime.typing.Member;
import com.tscript.runtime.typing.TObject;

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

            Thread debugThread = new Thread(() -> debugger.onHalt(getVMState(), this));
            debugThread.start();

            getCurrentThread().halt();
        }
    }

    private VMState getVMState() {
        return new VMStateImp(getCurrentThread().getVM());
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


    private static class VMStateImp implements VMState {

        private final List<ThreadState> threadStates = new ArrayList<>();

        public VMStateImp(TscriptVM vm) {
            for (TThread thread : vm.getThreads()) {
                threadStates.add(new ThreadStateImp(thread));
            }
        }

        @Override
        public List<ThreadState> getThreads() {
            return threadStates;
        }

        @Override
        public List<ObjectState> getGlobals() {
            return List.of();
        }
    }

    private static class ThreadStateImp implements ThreadState {

        private final List<FrameState> frameStates = new ArrayList<>();

        public ThreadStateImp(TThread thread) {
            for (Frame frame : thread.frameStack) {
                frameStates.add(new FrameStateImp(frame));
            }
        }

        @Override
        public List<FrameState> getFrames() {
            return frameStates;
        }
    }

    private static class FrameStateImp implements FrameState {

        private final String name;
        private final List<ObjectState> locals = new ArrayList<>();
        private final List<ObjectState> stack = new ArrayList<>();

        public FrameStateImp(Frame frame) {
            this.name = frame.getName();

            Map<TObject, ObjectState> buildCache = new HashMap<>();

            for (TObject object : frame.locals) {
                if (buildCache.containsKey(object)) {
                    locals.add(buildCache.get(object));
                }
                else {
                    locals.add(new ObjectStateImp(object, buildCache));
                }
            }

            for (TObject object : frame.stack) {
                if (buildCache.containsKey(object)) {
                    stack.add(buildCache.get(object));
                }
                else {
                    stack.add(new ObjectStateImp(object, buildCache));
                }
            }
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public List<ObjectState> getLocals() {
            return locals;
        }

        @Override
        public List<ObjectState> getStack() {
            return stack;
        }
    }

    private static class ObjectStateImp implements ObjectState {

        private final Map<String, ObjectState> members;

        public ObjectStateImp(TObject object, Map<TObject, ObjectState> buildCache) {
            this.members = new HashMap<>();

            for (Member member : object.getMembers()) {
                TObject obj = member.get();
                if (buildCache.containsKey(obj)) {
                    members.put(member.getName(), buildCache.get(obj));
                }
                else {
                    members.put(member.getName(), new ObjectStateImp(obj, buildCache));
                }
            }
        }

        @Override
        public Map<String, ObjectState> getMembers() {
            return members;
        }
    }
}
