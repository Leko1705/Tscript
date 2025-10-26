package com.tscript.runtime.jit.caching;

public interface JITCache {

    int size();

    CacheEntry get(String key);

    boolean remove(String key);

}
