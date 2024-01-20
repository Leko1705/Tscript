package runtime.heap;

import runtime.core.Reference;
import runtime.type.TObject;

import java.util.HashMap;

public class SimpleHeap implements Heap {

    private final HashMap<Reference, TObject> memory = new HashMap<>();

    @Override
    public Reference store(TObject object) {
        Reference ptr = new Reference();
        memory.put(ptr, object);
        return ptr;
    }

    @Override
    public TObject load(Reference ptr) {
        return memory.get(ptr);
    }

    @Override
    public int size() {
        return memory.size();
    }

    @Override
    public Reference[] getReferences() {
        return memory.keySet().toArray(new Reference[0]);
    }

    @Override
    public void free(Reference ptr) {
        memory.remove(ptr);
    }

}
