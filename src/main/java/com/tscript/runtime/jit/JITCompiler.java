package com.tscript.runtime.jit;

import com.tscript.runtime.jit.caching.JITCache;

public interface JITCompiler {

    int HOTNESS_THRESHOLD = 100;


    void schedule(JITTask task);

    JITCache getCache();

    default void boot(){
    }

    default void shutdown(){
    }

}
