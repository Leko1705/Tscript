package runtime.jit;

import runtime.core.VirtualFunction;
import runtime.type.Callable;

import java.io.Closeable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;

public class JIT extends Thread implements Closeable {

    private boolean running = true;

    private final LinkedBlockingDeque<JITTask> queue = new LinkedBlockingDeque<>();

    private final Map<String, Callable> optimized = new ConcurrentHashMap<>();

    public JIT(){
        start();
    }

    public Callable getOptimized(VirtualFunction callable){
        return optimized.get(callable.getName());
    }

    public boolean hasOptimization(String name){
        return optimized.containsKey(name);
    }

    protected void setOptimized(String name, Callable optimized) {
        this.optimized.put(name, optimized);
    }

    @Override
    public void run() {
        while (running){
            if (queue.isEmpty()) continue;
            JITTask task = queue.poll();
            task.handle(this);
        }
    }

    public void addTask(JITTask task){
        queue.add(task);
    }

    @Override
    public void close() {
        running = false;
    }

}
