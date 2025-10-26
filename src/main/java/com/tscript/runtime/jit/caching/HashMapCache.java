package com.tscript.runtime.jit.caching;

import java.util.HashMap;
import java.util.Map;

public class HashMapCache implements JITCache {

    private final Map<String, CacheEntry> map;

    public HashMapCache() {
        this.map = new HashMap<>();
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public CacheEntry get(String key) {
        return map.computeIfAbsent(key, k -> new CacheEntry());
    }

    @Override
    public boolean remove(String key) {
        return map.remove(key) != null;
    }
}
