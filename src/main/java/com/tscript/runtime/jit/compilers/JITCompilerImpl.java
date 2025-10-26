package com.tscript.runtime.jit.compilers;

import com.tscript.runtime.jit.JITCompiler;
import com.tscript.runtime.jit.JITTask;
import com.tscript.runtime.jit.caching.JITCache;

import java.util.Objects;

public record JITCompilerImpl(JITCache cache) implements JITCompiler {

    public JITCompilerImpl(JITCache cache) {
        this.cache = Objects.requireNonNull(cache);
    }

    @Override
    public void schedule(JITTask task) {
        task.run();
    }

    @Override
    public JITCache getCache() {
        return cache;
    }

}
