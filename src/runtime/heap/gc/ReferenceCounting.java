package runtime.heap.gc;

import runtime.core.Reference;
import runtime.heap.Heap;

import java.util.Collection;

public class ReferenceCounting implements GarbageCollector {
    @Override
    public void onAction(int threadID,
                         Heap heap,
                         Reference assigned,
                         Reference displaced,
                         Collection<Reference> roots) {
        assigned.incRC();
        displaced.decRC();
        if (displaced.getRC() <= 0)
            heap.free(displaced);
    }
}
