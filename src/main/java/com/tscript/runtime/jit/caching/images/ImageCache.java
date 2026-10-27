package com.tscript.runtime.jit.caching.images;

import com.tscript.runtime.jit.*;
import com.tscript.runtime.typing.TObject;
import com.tscript.runtime.typing.Type;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicInteger;

public class ImageCache {
    public static final JITCompiler.Key<ImageCache> KEY = ImageCache::new;



    public class Entry {
        private CompletableFuture<NativeImage> image = new CompletableFuture<>();
        private int hotness = 0;
        private int level = 0;

        public synchronized CompletableFuture<NativeImage> getImage() {
            return image;
        }

        public synchronized CompletableFuture<ParseProcess> use() {
            JITConfiguration config = compiler.getConfiguration();
            List<CompilationLevel> levels = config.optimizationLevels();

            if (level == levels.size()) return new CompletableFuture<>();

            hotness++;
            CompilationLevel lvl = levels.get(level);

            if (hotness > lvl.threshold()) {
                hotness = 0;
                level++;
                image = new CompletableFuture<>();
                ParseProcess pc = new ParseProcess(compiler, lvl, image);
                return CompletableFuture.completedFuture(pc);
            }

            return new CompletableFuture<>();
        }

    }


    private static class Node {
        public Entry entry;
        public final Map<Type, Node> children = new ConcurrentHashMap<>();
    }

    private static class RootNode extends Node {
        public AtomicInteger specializations = new AtomicInteger(0);
    }


    private final Set<Object> blacklist;
    private final Map<Object, RootNode> trees;
    private final JITCompiler compiler;

    public ImageCache(JITCompiler compiler) {
        this.compiler = compiler;
        trees = new ConcurrentHashMap<>();
        blacklist = new ConcurrentSkipListSet<>();
    }


    public Entry get(Object key, TObject... args) {
        if (blacklist.contains(key)) throw new IllegalStateException("Blacklisted key: " + key);
        RootNode rootNode = trees.computeIfAbsent(key, k -> new RootNode());

        Node node = rootNode;
        for (TObject arg : args) {
            Type type = arg.getType();
            node = node.children.computeIfAbsent(type, k -> new Node());
        }

        if (node.entry == null) {
            node.entry = new Entry();

            if (rootNode.specializations.incrementAndGet() > compiler.getConfiguration().specializationLimit()){
                blacklist(key);
            }
        }

        return node.entry;
    }

    public boolean isBlacklisted(Object key) {
        return blacklist.contains(key);
    }

    public void blacklist(Object key) {
        blacklist.add(key);
        trees.remove(key);
    }
}
