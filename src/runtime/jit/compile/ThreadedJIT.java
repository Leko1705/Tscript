package runtime.jit.compile;

import runtime.jit.table.LookUpTable;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

class ThreadedJIT extends Thread implements JITCompiler {

    private final JITCompiler JIT;

    private final BlockingDeque<Task> queue = new LinkedBlockingDeque<>();

    private volatile boolean active = true;

    public ThreadedJIT(JITCompiler jit) {
        JIT = jit;
        start();
    }

    @Override
    public LookUpTable getLookUpTable() {
        return JIT.getLookUpTable();
    }

    @Override
    public void handle(Task task) {
        queue.offer(task);
    }

    @Override
    public void run() {
        while (active){
            while (active && queue.isEmpty())
                Thread.onSpinWait();
            Task task = queue.poll();
            JIT.handle(task);
        }
    }

    @Override
    public void close() {
        active = false;
    }
}
