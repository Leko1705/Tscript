package runtime.debug;

import runtime.core.TThread;
import runtime.core.TscriptVM;

public interface Debugger {

    DebugAction onBreakPoint(TscriptVM vm, TThread thread);

}
