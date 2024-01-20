package runtime.heap.gc;

import runtime.core.Reference;
import runtime.heap.Heap;

import java.util.Collection;

public interface GarbageCollector {

    void onAction(int threadID,
                  Heap heap,
                  Reference assigned,
                  Reference displaced,
                  Collection<Reference> roots);

}
