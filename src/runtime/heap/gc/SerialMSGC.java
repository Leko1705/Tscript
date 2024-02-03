package runtime.heap.gc;

import runtime.core.Data;
import runtime.core.Reference;
import runtime.heap.Heap;
import runtime.type.*;

import java.util.*;

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

    @Override
    public GCType getType() {
        return GCType.TRACING;
    }

    private void mark(Heap heap, Collection<Reference> roots){
        Queue<Reference> queue = new ArrayDeque<>(roots);
        while (!queue.isEmpty()){
            Reference ptr = queue.poll();
            ptr.setMarked(true);
            TObject object = heap.load(ptr);

            if (object instanceof TArray a)
                traceArray(a, queue);
            else if (object instanceof TDictionary d)
                traceDict(d, queue);

            for (Member member : object.getMembers())
                if (member.data != null && member.data.isReference())
                    traceData(member.data, queue);
        }

    }

    private void traceArray(TArray array, Queue<Reference> queue){
        for (Data data : array.get())
            traceData(data, queue);
    }

    private void traceDict(TDictionary dictionary, Queue<Reference> queue){
        for (Map.Entry<Data, Data> entry : dictionary.get().entrySet()){
            traceData(entry.getKey(), queue);
            traceData(entry.getValue(), queue);
        }
    }

    private void traceData(Data data, Queue<Reference> queue){
        if (data.isReference()) queue.add(data.asReference());
        else if (data instanceof TArray a) traceArray(a, queue);
        else if (data instanceof TDictionary d) traceDict(d, queue);
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
