package runtime.heap;

import runtime.core.Reference;
import runtime.debug.Debuggable;
import runtime.debug.HeapInfo;
import runtime.type.TObject;

public interface Heap extends Debuggable<HeapInfo> {

    String getName();

    Reference store(TObject object);

    TObject load(Reference ptr);

    int size();

    Reference[] getReferences();

    void free(Reference ptr);

    default void onSurvive(Reference ptr){}

    HeapInfo loadInfo(Heap heap);
}
