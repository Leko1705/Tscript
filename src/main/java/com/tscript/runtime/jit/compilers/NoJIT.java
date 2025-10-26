package com.tscript.runtime.jit.compilers;

import com.tscript.runtime.jit.JITCompiler;
import com.tscript.runtime.jit.JITTask;
import com.tscript.runtime.jit.NativeImage;
import com.tscript.runtime.jit.caching.CacheEntry;
import com.tscript.runtime.jit.caching.HashMapCache;
import com.tscript.runtime.jit.caching.JITCache;
import com.tscript.runtime.typing.Type;

public class NoJIT implements JITCompiler {
    
    private static final JITCache cache = new NoCache();

    @Override
    public void schedule(JITTask task) {
    }

    @Override
    public JITCache getCache() {
        return cache;
    }


    private static class NoCache implements JITCache {
        @Override
        public int size() {
            return 0;
        }

        @Override
        public CacheEntry get(String key) {
            return new CacheEntry();
        }

        @Override
        public boolean remove(String key) {
            return false;
        }


    }
}
