package runtime.heap;

import runtime.core.Reference;
import runtime.type.TObject;

public interface Heap {

    Reference store(TObject object);

    TObject load(Reference ptr);

    int size();

    Reference[] getReferences();

    void free(Reference ptr);

    default void onSurvive(Reference ptr){}

}
