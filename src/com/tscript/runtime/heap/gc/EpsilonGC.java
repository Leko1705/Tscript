package com.tscript.runtime.heap.gc;

import com.tscript.runtime.core.Reference;
import com.tscript.runtime.heap.Heap;

import java.util.Collection;

public class EpsilonGC implements GarbageCollector {
    @Override
    public void onAction(int threadID,
                         Heap heap,
                         Reference assigned,
                         Reference displaced,
                         Collection<Reference> roots) {
    }

    @Override
    public GCType getType() {
        return GCType.COUNTING; // for faster start-up
    }
}
