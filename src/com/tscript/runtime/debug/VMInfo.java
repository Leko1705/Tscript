package com.tscript.runtime.debug;

import java.util.List;

public interface VMInfo extends DebugInfo {

    List<ThreadInfo> getThreadTrees();

    HeapInfo getHeapTree();

    @Override
    default <P, R> R process(DebugHandler<P, R> handler, P p) {
        return handler.handleVMInfo(this, p);
    }
}
