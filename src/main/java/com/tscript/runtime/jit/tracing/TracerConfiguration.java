package com.tscript.runtime.jit.tracing;

import com.tscript.runtime.core.TThread;
import com.tscript.runtime.jit.JITTask;
import com.tscript.runtime.jit.ir.IRBlock;

public interface TracerConfiguration {

    boolean isFunctionFocused();

    default boolean isLoopFocused() {
        return !isFunctionFocused();
    }

    boolean isTraceComplete(TThread thread);

    boolean isParameterStorage();

    JITTask completeTrace(TThread thread, IRBlock block);

}
