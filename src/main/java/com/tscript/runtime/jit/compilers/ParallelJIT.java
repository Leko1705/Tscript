package com.tscript.runtime.jit.compilers;

import com.tscript.runtime.jit.JITCompiler;
import com.tscript.runtime.jit.JITTask;
import com.tscript.runtime.jit.caching.JITCache;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public class ParallelJIT implements JITCompiler {

    private final JITCompiler underlying;
    private final Handler handler;


    public ParallelJIT(JITCompiler underlying) {
        this.underlying = underlying;
        this.handler = new Handler();
    }


    @Override
    public void schedule(JITTask task) {
        handler.queue.add(task);
    }

    @Override
    public JITCache getCache() {
        return underlying.getCache();
    }

    @Override
    public void boot() {
        underlying.boot();
        handler.start();
    }

    @Override
    public void shutdown() {
        underlying.shutdown();
        handler.running = false;
    }


    private static class Handler extends Thread {

        private volatile boolean running = true;
        private final Queue<JITTask> queue = new LinkedBlockingQueue<>();

        @Override
        public void run() {
            while (running) {
                if (queue.isEmpty()) continue;
                JITTask task = queue.poll();
                task.run();
            }
        }
    }

}
