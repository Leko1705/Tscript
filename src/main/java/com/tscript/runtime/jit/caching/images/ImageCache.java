package com.tscript.runtime.jit.caching.images;

import com.tscript.runtime.jit.*;
import com.tscript.runtime.jit.comp.ir.Graph;
import com.tscript.runtime.jit.comp.levels.OptimizingLevel;
import com.tscript.runtime.typing.TObject;
import com.tscript.runtime.typing.Type;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Stores the compiled images of functions and loops.
 * <p>
 * Function images are specialized on the runtime types of their arguments: every key
 * (function metadata or {@link com.tscript.runtime.jit.tracing.LoopKey}) owns a tree of entries
 * indexed by the argument types. Each entry tracks its hotness and walks through the
 * configured {@link CompilationLevel}s.
 */
public class ImageCache {
    public static final JITCompiler.Key<ImageCache> KEY = ImageCache::new;


    /** graphs with at most this many effects are recompiled by every level, even without changes */
    private static final int SMALL_GRAPH = 50;

    public class Entry {

        private final Object key;
        private volatile NativeImage image = null;
        private int hotness = 0;
        private int level = 0;
        private boolean busy = false;
        /** true if the image was invalidated while a compilation was running (its result is discarded) */
        private boolean stale = false;
        /** the number of side traces stitched into the current image */
        private int stitched = 0;
        private boolean disabled = false;
        private int failures = 0;
        /** true once no further compilation will happen for this entry (allows a lock free fast path) */
        private volatile boolean saturated = false;
        private int deopts = 0;
        private int invalidations = 0;
        /** the recorded graph; later compilation levels recompile it instead of recording a new trace */
        private Graph graph = null;

        private Entry(Object key) {
            this.key = key;
        }

        public Object getKey() {
            return key;
        }

        @Override
        public String toString() {
            if (key instanceof com.tscript.runtime.core.VirtualFunction.VirtualMetadata m) return "function " + m.name();
            return String.valueOf(key);
        }

        /**
         * @return the current image or null if none is available
         */
        public NativeImage image() {
            return image;
        }

        /**
         * @return the recorded graph of this entry if it was compiled successfully, null otherwise
         */
        public synchronized Graph compiledGraph() {
            return image != null && !disabled ? graph : null;
        }

        /**
         * Increments the hotness of this entry. If the next compilation level is reached and a
         * graph was recorded before, it is recompiled right away.
         * @return a parse process if a trace for the next compilation level should be recorded now
         */
        public ParseProcess use() {
            if (saturated) return null;
            return use0();
        }

        private synchronized ParseProcess use0() {
            if (disabled || busy) return null;
            List<CompilationLevel> levels = compiler.getConfiguration().optimizationLevels();
            if (level >= levels.size()) {
                saturated = true;
                return null;
            }

            CompilationLevel lvl = levels.get(level);
            if (++hotness < lvl.threshold()) return null;

            hotness = 0;
            if (graph != null && !lvl.retrace()) {
                int sideTraces = countSideTraces(lvl, graph);
                if (sideTraces == stitched && graph.effects().size() > SMALL_GRAPH) {
                    // recompiling would produce the same code (no new side traces to stitch). For
                    // small graphs it still pays off (the JVM compiles the fresh class with the
                    // profile of the warmed up program), for large ones the JVM compilation costs
                    // more than it gains.
                    compiler.debug(() -> "skipped level " + (level + 1) + " of " + this + " (nothing new to stitch)");
                    level++;
                    saturated = level >= levels.size();
                    return null;
                }
                stitched = sideTraces;
                busy = true;
                new ParseProcess(compiler, lvl, this, graph).compile();
                return null;
            }
            busy = true;
            stitched = 0;
            return new ParseProcess(compiler, lvl, this, new Graph());
        }

        private int countSideTraces(CompilationLevel level, Graph graph) {
            if (!(level instanceof OptimizingLevel optimizing)) return 0;
            try {
                return optimizing.countSideTraces(graph, ImageCache.this::sideTraceGraph);
            }
            catch (RuntimeException e) {
                // the graph of a side trace is being optimized concurrently (background compilation)
                return -1;
            }
        }

        public synchronized void onCompiled(NativeImage image, Graph graph) {
            if (stale) {
                stale = false;
                busy = false;
                return;
            }
            this.graph = graph;
            compiler.debug(() -> "compiled " + this + " at level " + (level + 1));
            this.image = image;
            this.level++;
            this.saturated = level >= compiler.getConfiguration().optimizationLevels().size();
            this.busy = false;
            this.deopts = 0;
        }

        /**
         * Called if recording or compiling a trace failed.
         */
        public synchronized void onFailed() {
            busy = false;
            stale = false;
            if (++failures > compiler.getConfiguration().failureLimit()) {
                compiler.debug(() -> "disabled " + this);
                disabled = true;
                saturated = true;
            }
        }

        /**
         * Called whenever the image of this entry deoptimized. If an image misspeculates too often
         * it is thrown away so that a new trace (with the current type profile) can be recorded.
         */
        public synchronized void onDeopt(boolean loopExit) {
            if (loopExit || image == null) return;
            if (++deopts > compiler.getConfiguration().deoptLimit()) {
                compiler.debug(() -> "invalidated image of " + this + " (too many deoptimizations)");
                image = null;
                graph = null;
                level = 0;
                stale = busy;
                saturated = false;
                hotness = 0;
                deopts = 0;
                if (++invalidations > compiler.getConfiguration().failureLimit()) {
                    disabled = true;
                    saturated = true;
                }
            }
        }

        public synchronized boolean isDisabled() {
            return disabled;
        }
    }


    private static class Node {
        public volatile Entry entry;
        public final Map<Type, Node> children = new ConcurrentHashMap<>();
    }

    private static class RootNode extends Node {
        public final AtomicInteger specializations = new AtomicInteger(0);
        /** the most recently used specialization (monomorphic fast path) */
        public volatile LastHit last = null;
    }

    private record LastHit(Type[] types, Entry entry) {}


    private final Set<Object> blacklist;
    private final Map<Object, RootNode> trees;
    private final JITCompiler compiler;

    public ImageCache(JITCompiler compiler) {
        this.compiler = compiler;
        trees = new ConcurrentHashMap<>();
        blacklist = ConcurrentHashMap.newKeySet();
    }

    /**
     * @return the entry for the given key and argument types or null if the key is blacklisted
     */
    public Entry get(Object key, TObject... args) {
        RootNode rootNode = trees.get(key);
        if (rootNode != null) {
            LastHit last = rootNode.last;
            if (last != null && matches(last.types, args)) return last.entry;
        }
        if (blacklist.contains(key)) return null;
        if (rootNode == null) rootNode = trees.computeIfAbsent(key, k -> new RootNode());

        Node node = rootNode;
        for (TObject arg : args) {
            node = node.children.computeIfAbsent(arg.getType(), k -> new Node());
        }

        Entry entry = node.entry;
        if (entry == null) {
            synchronized (node) {
                if (node.entry == null) {
                    if (rootNode.specializations.incrementAndGet() > compiler.getConfiguration().specializationLimit()) {
                        blacklist(key);
                        return null;
                    }
                    node.entry = new Entry(key);
                }
                entry = node.entry;
            }
        }

        Type[] types = new Type[args.length];
        for (int i = 0; i < args.length; i++) types[i] = args[i].getType();
        rootNode.last = new LastHit(types, entry);
        return entry;
    }

    private static boolean matches(Type[] types, TObject[] args) {
        if (types.length != args.length) return false;
        for (int i = 0; i < args.length; i++) {
            if (types[i] != args[i].getType()) return false;
        }
        return true;
    }

    /**
     * @return the compiled graph of the side trace attached to the given exit, or null
     */
    public Graph sideTraceGraph(Object exit) {
        RootNode root = trees.get(exit);
        if (root == null) return null;
        Entry entry = root.entry;
        return entry == null ? null : entry.compiledGraph();
    }

    public boolean isBlacklisted(Object key) {
        return blacklist.contains(key);
    }

    public void blacklist(Object key) {
        blacklist.add(key);
        trees.remove(key);
    }
}
