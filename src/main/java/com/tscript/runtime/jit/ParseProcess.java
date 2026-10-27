package com.tscript.runtime.jit;

import com.tscript.runtime.jit.comp.ir.Graph;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class ParseProcess {

    private final Graph graph;

    private final JITCompiler compiler;
    private final CompilationLevel level;
    private final CompletableFuture<NativeImage> futureImage;

    public ParseProcess(JITCompiler compiler,
                        CompilationLevel level,
                        CompletableFuture<NativeImage> futureImage) {
        this.graph = new Graph();
        this.compiler = Objects.requireNonNull(compiler);
        this.level = Objects.requireNonNull(level);
        this.futureImage = Objects.requireNonNull(futureImage);
    }

    public Graph getGraph() {
        return graph;
    }

    public void compile() {
        CompileTask task = level.prepare(graph);
        compiler.schedule(task).thenAccept(futureImage::complete);
    }
}
