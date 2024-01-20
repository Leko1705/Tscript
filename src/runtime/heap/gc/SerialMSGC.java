package runtime.heap.gc;

import runtime.core.Reference;
import runtime.heap.Heap;
import runtime.type.Member;
import runtime.type.TObject;

import java.util.ArrayDeque;
import java.util.Collection;

public class SerialMSGC implements GarbageCollector {

    @Override
    public void onAction(int threadID,
                         Heap heap,
                         Reference assigned,
                         Reference displaced,
                         Collection<Reference> roots) {
        mark(heap, roots);
        sweep(heap);
    }

    private void mark(Heap heap, Collection<Reference> roots){
        ArrayDeque<Reference> queue = new ArrayDeque<>(roots);
        while (!queue.isEmpty()){
            Reference ptr = queue.poll();
            ptr.setMarked(true);
            TObject object = heap.load(ptr);

            for (Member member : object.getMembers())
                if (member.data.isReference())
                    queue.add(member.data.asReference());
        }

    }

    private void sweep(Heap heap){
        for (Reference pointer : heap.getReferences()) {
            if (!pointer.isMarked()) {
                heap.free(pointer);
            }
            else {
                pointer.setMarked(false);
                heap.onSurvive(pointer);
            }
        }

    }

}
