package com.tscript.runtime.heap.gc;

import com.tscript.runtime.core.Reference;
import com.tscript.runtime.heap.Heap;

import java.util.Collection;

public interface GarbageCollector {

    void onAction(int threadID,
                  Heap heap,
                  Reference assigned,
                  Reference displaced,
                  Collection<Reference> roots);

    GCType getType();

}
