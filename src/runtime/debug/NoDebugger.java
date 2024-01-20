package runtime.debug;

import runtime.core.TThread;
import runtime.core.TscriptVM;

public class NoDebugger implements Debugger {
    @Override
    public DebugAction onBreakPoint(TscriptVM vm, TThread thread) {
        return DebugAction.RESUME;
    }
}
