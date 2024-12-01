package com.tscript.runtime.jit;

import com.tscript.runtime.jit.graph.Node;

import java.util.concurrent.CompletableFuture;

public interface JITCompiler {

    CompletableFuture<NativeImage> compile(Node instructions);

}
