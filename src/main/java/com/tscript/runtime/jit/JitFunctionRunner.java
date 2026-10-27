package com.tscript.runtime.jit;

import com.tscript.runtime.core.VirtualFrame;
import com.tscript.runtime.core.TThread;

public class JitFunctionRunner {

    public static String getLoopKey(TThread thread, int fallbackAddress) {
        VirtualFrame frame = thread.topVirtualFrame();
        return "loop::" + frame.getModule().getCanonicalPath() + ":" + frame.getName() + ":" + fallbackAddress;
    }

}
