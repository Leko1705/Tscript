package com.tscript.runtime.jit;

import com.tscript.runtime.jit.caching.images.ImageCache;
import com.tscript.runtime.jit.comp.ir.Graph;

import java.util.Objects;

/**
 * The life cycle of one compilation: a front end (the tracer) builds the graph, then
 * {@link #compile()} hands it to the compilation level, or {@link #abort()} gives up.
 */
public class ParseProcess {

    private final Graph graph;

    private final JITCompiler compiler;
    private final CompilationLevel level;
    private final ImageCache.Entry entry;

    public ParseProcess(JITCompiler compiler,
                        CompilationLevel level,
                        ImageCache.Entry entry) {
        this.graph = new Graph();
        this.compiler = Objects.requireNonNull(compiler);
        this.level = Objects.requireNonNull(level);
        this.entry = Objects.requireNonNull(entry);
    }

    public Graph getGraph() {
        return graph;
    }

    public JITCompiler getCompiler() {
        return compiler;
    }

    public ImageCache.Entry getEntry() {
        return entry;
    }

    public void compile() {
        CompileTask task = level.prepare(graph);
        compiler.schedule(task).whenComplete((image, error) -> {
            if (error != null || image == null) {
                compiler.debug(() -> "compilation of " + entry + " failed: " + error);
                if (error != null && compiler.getConfiguration().debug()) error.printStackTrace(System.err);
                entry.onFailed();
            }
            else {
                entry.onCompiled(image);
            }
        });
    }

    public void abort(String reason) {
        compiler.debug(() -> "trace of " + entry + " aborted: " + reason);
        entry.onFailed();
    }
}
