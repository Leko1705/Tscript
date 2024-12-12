package com.tscript.runtime.jit.compilers;

import com.tscript.runtime.jit.JITCompiler;
import com.tscript.runtime.jit.NativeImage;
import com.tscript.runtime.jit.graph.Node;
import com.tscript.runtime.typing.Type;

import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;

public class ParallelJIT implements JITCompiler {

    private record Task(Node node, Type[] types, CompletableFuture<NativeImage> image){}


    private final JITCompiler underlying;

    private Handler handler;


    public ParallelJIT(JITCompiler underlying) {
        this.underlying = underlying;
    }


    @Override
    public CompletableFuture<NativeImage> compile(Node graph, Type[] types) {
        CompletableFuture<NativeImage> future = new CompletableFuture<>();
        handler.queue.add(new Task(graph, types, future));
        return future;
    }


    @Override
    public void boot() {
        underlying.boot();
        handler = new Handler();
        handler.start();
    }

    @Override
    public void shutdown() {
        underlying.shutdown();
        handler.running = false;
    }


    private class Handler extends Thread {

        private volatile boolean running = true;
        private final Queue<Task> queue = new LinkedBlockingQueue<>();

        @Override
        public void run() {
            while (running) {
                if (queue.isEmpty()) continue;
                Task task = queue.poll();
                try {
                    NativeImage img = underlying.compile(task.node, task.types).get();
                    task.image.complete(img);
                }
                catch (Exception ignored) {}
            }
        }
    }

}
