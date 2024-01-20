package runtime.heap;

import runtime.core.Reference;
import runtime.type.TObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.PriorityQueue;

public class GenerationalHeap implements Heap {

    private static final int NEWCOMER_GEN = 0;

    private final ArrayList<HashMap<Reference, TObject>> generations;

    private final HashMap<Reference, TObject> all = new HashMap<>();

    private final PriorityQueue<HashMap<Reference, TObject>> PQ = new PriorityQueue<>(new GenerationComparator());

    public GenerationalHeap(int generations){
        if (generations <= 1)
            throw new IllegalArgumentException("GC needs at least two generations");
        this.generations = new ArrayList<>(generations);
        for (int i = 0; i < generations; i++) {
            HashMap<Reference, TObject> gen = new HashMap<>();
            this.generations.add(gen);
            PQ.add(gen);
        }
    }

    @Override
    public Reference store(TObject object) {
        Reference ptr = new Reference();
        HashMap<Reference, TObject> newComers = generations.get(NEWCOMER_GEN);
        newComers.put(ptr, object);
        PQ.add(PQ.remove());
        all.put(ptr, object);
        return ptr;
    }

    @Override
    public TObject load(Reference ptr) {
        return all.get(ptr);
    }

    @Override
    public int size() {
        return all.size();
    }

    @Override
    public Reference[] getReferences() {
        return PQ.element().keySet().toArray(new Reference[0]);
    }

    @Override
    public void free(Reference ptr) {
        all.remove(ptr);
        HashMap<Reference, TObject> gen = PQ.remove();
        gen.remove(ptr);
        PQ.add(gen);
    }

    @Override
    public void onSurvive(Reference ptr) {
        for (int i = 0; i < generations.size(); i++){
            HashMap<Reference, TObject> gen = generations.get(i);
            if (gen.containsKey(ptr)) {
                TObject object = gen.remove(ptr);
                int nextGenIndex = Math.min(i+1, generations.size()-1);
                HashMap<Reference, TObject> newGen = generations.get(nextGenIndex);
                newGen.put(ptr, object);
                break;
            }
        }

    }


    private static class GenerationComparator implements Comparator<HashMap<Reference, TObject>> {

        @Override
        public int compare(HashMap<Reference, TObject> o1, HashMap<Reference, TObject> o2) {
            return Integer.compare(o2.size(), o1.size()); // max heap
        }
    }
}
