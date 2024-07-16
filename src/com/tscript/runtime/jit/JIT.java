package com.tscript.runtime.jit;

import com.tscript.runtime.core.Data;
import com.tscript.runtime.core.VirtualFunction;
import com.tscript.runtime.heap.Heap;
import com.tscript.runtime.type.Callable;
import com.tscript.runtime.type.TObject;

import java.io.Closeable;
import java.util.*;
import java.util.concurrent.LinkedBlockingDeque;

public class JIT extends Thread implements Closeable {

    public static final boolean enabled = true;
    public static final int HOTSPOT_THRESHOLD = 100_000;


    private boolean running = true;

    private final Map<String, Integer> hotness = new HashMap<>();

    private final LinkedBlockingDeque<JITTask> queue = new LinkedBlockingDeque<>();

    private final OptimTable table = new OptimTable();

    private final Heap heap;

    public JIT(Heap heap){
        this.heap = heap;
        if (!enabled) return;
        start();
    }

    public Callable getOptimized(VirtualFunction function, Data[] params){
        return table.load(function.getName(), params);
    }

    public boolean hasOptimization(VirtualFunction function, Data[] params){
        return getOptimized(function, params) != null;
    }

    protected void setOptimized(VirtualFunction function, Callable optimized, Data[] args) {
        table.store(optimized, function.getName(), args);
    }

    @Override
    public void run() {
        while (running){
            if (queue.isEmpty()) continue;
            JITTask task = queue.poll();
            task.handle(this);
        }
    }

    public void addTask(JITTask task){
        if (!enabled) return;
        queue.add(task);
    }

    @Override
    public void close() {
        running = false;
    }

    public boolean isHot(String name) {
        if (!hotness.containsKey(name)) {
            hotness.put(name, 0);
            return false;
        }
        int hotness = this.hotness.get(name);
        if (hotness >= HOTSPOT_THRESHOLD)
            return true;
        this.hotness.put(name, hotness+1);
        return false;
    }


    private class OptimTable {

        private final Set<Integer> hashes = new HashSet<>();
        private final Map<String, Node> roots = new HashMap<>();

        public Callable load(String name, Data[] args){
            if (!hashes.contains(Objects.hash((Object[]) args)))
                return null;
            Node node = roots.get(name);
            if (node == null) return null;
            return node.load(args, 0);
        }

        public void store(Callable optimized, String name, Data[] args){
            hashes.add(Objects.hash((Object[]) args));
            if (roots.containsKey(name)){
                Node node = roots.get(name);
                node.store(optimized, args, 0);
            }
            else {
                if (args.length == 0){
                    roots.put(name, new LeafNode(optimized));
                }
                else {
                    SearchNode searchNode = new SearchNode();
                    searchNode.store(optimized, args, 0);
                    roots.put(name, searchNode);
                }
            }
        }

        private interface Node {
            Callable load(Data[] args, int idx);
            void store(Callable optimized, Data[] args, int idx);
        }

        private class SearchNode implements Node {
            private final Map<String, Node> children = new HashMap<>();
            public Callable load(Data[] args, int idx){
                Node successor = children.get(typeName(args[idx]));
                if (successor == null) return null;
                return successor.load(args, idx+1);
            }

            @Override
            public void store(Callable optimized, Data[] args, int idx) {
                if (idx == args.length-1)
                    children.put(typeName(args[idx]), new LeafNode(optimized));
                else {
                    Node successor = children.get(typeName(args[idx]));
                    if (successor == null){
                        successor = new SearchNode();
                        children.put(typeName(args[idx]), successor);
                    }
                    successor.store(optimized, args, idx+1);
                }
            }
        }

        private record LeafNode(Callable callable) implements Node {
            @Override
            public Callable load(Data[] args, int idx) {
                if (idx < args.length) return null;
                return callable;
            }

            @Override
            public void store(Callable optimized, Data[] args, int idx) {
                throw new UnsupportedOperationException("store");
            }

        }

    }

    private String typeName(Data data){
        return unpack(data).getType().getName();
    }

    protected TObject unpack(Data data){
        if (data.isReference()){
            return heap.load(data.asReference());
        }
        return data.asValue();
    }

}
