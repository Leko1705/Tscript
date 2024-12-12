package com.tscript.runtime.jit.compilers;

import com.tscript.runtime.jit.JITCompiler;
import com.tscript.runtime.jit.NativeImage;
import com.tscript.runtime.jit.graph.Node;
import com.tscript.runtime.typing.Type;

import java.util.concurrent.CompletableFuture;

public class NoJIT implements JITCompiler {
    @Override
    public CompletableFuture<NativeImage> compile(Node graph, Type[] types) {
        return new CompletableFuture<>();
    }
}
