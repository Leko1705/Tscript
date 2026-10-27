package com.tscript.runtime.jit.schedule;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Compiles on a background (daemon) thread, so the interpreter keeps running while a trace is
 * compiled. Tasks are executed one after another in submission order.
 */
public class BackgroundScheduler implements Scheduler {

    private final ExecutorService executor = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "tscript-jit-compiler");
        thread.setDaemon(true);
        return thread;
    });

    @Override
    public void schedule(Runnable runnable) {
        executor.execute(runnable);
    }

    @Override
    public void shutdown() {
        executor.shutdownNow();
    }
}
