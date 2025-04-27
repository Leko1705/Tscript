package com.tscript.runtime.jit;

import com.tscript.runtime.jit.graph.Node;
import com.tscript.runtime.typing.Type;

import java.util.concurrent.CompletableFuture;

public interface JITCompiler {

    CompletableFuture<NativeImage> compile(Node instructions, Type[] inputTypes);

    void handle(JITTask task);

    default void boot(){
    }

    default void shutdown(){
    }

}
