package com.tscript.runtime.debug;

import com.tscript.runtime.heap.Heap;

public interface Debuggable<I extends DebugInfo> {

    I loadInfo(Heap heap);
    
}
