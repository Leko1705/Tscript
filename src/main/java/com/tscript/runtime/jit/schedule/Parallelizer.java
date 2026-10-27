package com.tscript.runtime.jit.schedule;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public record Parallelizer(ExecutorService executor) implements Scheduler {

    public Parallelizer(ExecutorService executor) {
        this.executor = Objects.requireNonNull(executor);
    }

    public Parallelizer() {
        this(Executors.newSingleThreadExecutor());
    }

    @Override
    public void schedule(Runnable runnable) {
        executor.submit(runnable);
    }

    @Override
    public void shutdown() {
        executor.close();
    }
}
