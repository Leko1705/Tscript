package runtime.debug;

public abstract class Debugger {

    private static final Debugger VOID_DEBUGGER = new Debugger() {
        @Override
        public void onDebug(int threadID, VMInfo info) {
            resume();
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
        action = null;

        onDebug(threadID, vmInfo);

        while (action == null)
            Thread.onSpinWait();

        return action;
    }

    public abstract void onDebug(int threadID, VMInfo info);


    public void step(){
        action = DebugAction.STEP;
    }

    public void resume(){
        action = DebugAction.RESUME;
    }

    public void quit(){
        action = DebugAction.QUIT;
    }

}
