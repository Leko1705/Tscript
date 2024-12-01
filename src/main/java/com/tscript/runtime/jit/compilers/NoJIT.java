package com.tscript.runtime.jit.compilers;

import com.tscript.runtime.jit.JITCompiler;
import com.tscript.runtime.jit.NativeImage;
import com.tscript.runtime.jit.graph.Node;

import java.util.concurrent.CompletableFuture;

public class NoJIT implements JITCompiler {
    @Override
    public CompletableFuture<NativeImage> compile(Node graph) {
        return new CompletableFuture<>();
    }
}
