package com.tscript.runtime.jit.tracing;

import com.tscript.runtime.jit.JITCompiler;

import java.util.HashMap;
import java.util.Map;

public class Snapshots {
    public static final JITCompiler.Key<Snapshots> KEY = k -> new Snapshots();


    private int nextId;
    private final Map<Integer, SnapshotShape> shapes;

    public Snapshots() {
        nextId = 0;
        this.shapes = new HashMap<>();
    }

    public SnapshotShape get(int id) {
        return shapes.get(id);
    }

    public int put(SnapshotShape shape) {
        int id = nextId++;
        shapes.put(id, shape);
        return id;
    }

}
