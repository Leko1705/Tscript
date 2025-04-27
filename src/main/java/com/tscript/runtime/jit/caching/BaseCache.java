package com.tscript.runtime.jit.caching;


import com.tscript.runtime.jit.NativeImage;
import com.tscript.runtime.typing.Type;

import java.util.HashMap;
import java.util.Map;

public class BaseCache implements JITCache {


    private class FunctionalNode {
        NativeImage image = null;
        Map<Type, FunctionalNode> children = new HashMap<>();

        public NativeImage search(Type[] types, int idx) {
            if (idx == types.length) return image;
            Type type = types[idx];
            FunctionalNode node = children.get(type);
            if (node == null) return null;
            return node.search(types, idx + 1);
        }

        private void put(Type[] types, int idx, NativeImage image) {
            if (idx == types.length) {
                if (this.image == null) size++;
                this.image = image;
                return;
            }
            FunctionalNode child = children
                    .computeIfAbsent(types[idx], k -> new FunctionalNode());
            child.put(types, idx + 1, image);
        }

        private boolean remove(Type[] types, int idx) {
            if (idx == types.length) {
                if (this.image == null) return false;
                size--;
                this.image = null;
                return true;
            }
            FunctionalNode child = children
                    .computeIfAbsent(types[idx], k -> new FunctionalNode());
            boolean res = child.remove(types, idx + 1);
            if (!res) return false;
            if (child.image == null && child.children.isEmpty())
                children.remove(types[idx]);
            return true;
        }
    }


    private int size;
    private final Map<String, Map<Integer, NativeImage>> positional;
    private final Map<String, FunctionalNode> functional;

    public BaseCache() {
        this.size = 0;
        this.positional = new HashMap<>();
        this.functional = new HashMap<>();
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public NativeImage getFunctional(String context, Type[] argumentTypes) {
        FunctionalNode node = functional.get(context);
        if (node == null) return null;
        return node.search(argumentTypes, 0);
    }

    @Override
    public void putFunctional(String context, Type[] argumentTypes, NativeImage image) {
        functional
                .computeIfAbsent(context, k -> new FunctionalNode())
                .put(argumentTypes, 0, image);
    }

    @Override
    public boolean removeFunctional(String context, Type[] argumentTypes) {
        FunctionalNode node = functional.get(context);
        if (node == null) return false;
        boolean res = node.remove(argumentTypes, 0);
        if (!res) return false;
        if (node.image == null && node.children.isEmpty())
            functional.remove(context);
        return true;
    }

    @Override
    public NativeImage getPositional(String context, int ip) {
        Map<Integer, NativeImage> map = positional.get(context);
        if (map == null) return null;
        return map.get(ip);
    }

    @Override
    public void putPositional(String context, int ip, NativeImage image) {
        Map<Integer, NativeImage> map = positional.computeIfAbsent(context, k -> new HashMap<>());
        if (!map.containsKey(ip)) size++;
        map.put(ip, image);
    }

    @Override
    public boolean removePositional(String context, int ip) {
        Map<Integer, NativeImage> map = positional.get(context);
        if (map == null || !map.containsKey(ip)) return false;
        size--;
        map.remove(ip);
        if (map.isEmpty()) positional.remove(context);
        return true;
    }

}
