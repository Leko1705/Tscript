package com.tscript.runtime.jit;

import com.tscript.runtime.jit.caching.JITCache;
import com.tscript.runtime.jit.graph.Node;
import com.tscript.runtime.typing.Type;

import java.util.concurrent.CompletableFuture;

public interface JITCompiler {

    void handle(JITTask task);

    JITCache getCache();

    default void boot(){
    }

    default void shutdown(){
    }

}
