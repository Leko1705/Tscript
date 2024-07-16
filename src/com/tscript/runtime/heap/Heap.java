package com.tscript.runtime.heap;

import com.tscript.runtime.core.Reference;
import com.tscript.runtime.debug.Debuggable;
import com.tscript.runtime.debug.HeapInfo;
import com.tscript.runtime.type.TObject;

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
