package com.tscript.runtime.heap;

import com.tscript.runtime.core.Reference;
import com.tscript.runtime.debug.DataInfo;
import com.tscript.runtime.debug.HeapInfo;
import com.tscript.runtime.type.TObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class SimpleHeap implements Heap {

    private final HashMap<Reference, TObject> memory = new HashMap<>();

    @Override
    public String getName() {
        return "simpleHeap";
    }

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

    @Override
    public HeapInfo loadInfo(Heap heap) {
        return new HeapInfo() {
            @Override
            public String getName() {
                return SimpleHeap.this.getName();
            }

            @Override
            public Collection<DataInfo> getData() {
                Collection<DataInfo> dataInfos = new ArrayList<>();
                for (TObject obj : memory.values())
                    dataInfos.add(obj.loadInfo(SimpleHeap.this));
                return dataInfos;
            }
        };
    }

}
