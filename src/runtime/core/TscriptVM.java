package runtime.core;

import runtime.debug.Debugger;
import runtime.debug.NoDebugger;
import runtime.heap.GenerationalHeap;
import runtime.heap.Heap;
import runtime.heap.SimpleHeap;
import runtime.heap.gc.GarbageCollector;
import runtime.heap.gc.SerialMSGC;
import runtime.jit.JIT;
import runtime.type.Callable;

import java.io.File;
import java.io.OutputStream;
import java.util.*;

public class TscriptVM {

    public static int run(File file, OutputStream out, OutputStream err){
        return run(file, out, err, null);
    }

    public static int run(File file, OutputStream out, OutputStream err, Debugger debugger){
        TscriptVM vm = new TscriptVM(out, err, new GenerationalHeap(4), new SerialMSGC(), debugger);
        return vm.execute(file);
    }

    public final OutputStream out;
    public final OutputStream err;


    private Data[] globals;

    private final Heap heap;

    private final GarbageCollector gc;

    private final Debugger debugger;

    private final JIT jit;

    private final Queue<Integer> freeThreadIDQueue = new ArrayDeque<>();
    private final HashMap<Integer, TThread> threads = new HashMap<>();
    private final Set<Reference> roots = new HashSet<>();


    protected TscriptVM(OutputStream out, OutputStream err, Heap heap, GarbageCollector gc, Debugger debugger){
        this.out = out;
        this.err = err;
        this.heap = heap;
        this.gc = gc;
        this.debugger = debugger != null ? debugger : new NoDebugger();
        this.jit = new JIT();
    }



    private int execute(File file){
        FileLoader fileLoader = new FileLoader(file, this);
        fileLoader.load();
        Pool pool = fileLoader.getPool();
        int entry = fileLoader.getEntryPoint();
        globals = new Data[fileLoader.getGlobals()];
        VirtualFunction mainFunction = (VirtualFunction) pool.load(entry, null);
        startNewThread(mainFunction);
        while (!threads.isEmpty())
            Thread.onSpinWait();
        jit.close();
        return 0;
    }

    public synchronized void startNewThread(Callable callable){
        int nextID = freeThreadIDQueue.isEmpty()
                ? threads.size()
                : freeThreadIDQueue.poll();

        TThread thread = new TThread(this, callable, nextID);
        threads.put(nextID, thread);
        thread.start();
    }

    public synchronized void killThread(int id){
        TThread thread = threads.remove(id);
        if (thread != null)
            thread.interrupt();
    }

    public synchronized Data storeGlobal(int addr, Data data){
        Data displaced = globals[addr];
        globals[addr] = data;
        return displaced;
    }

    public synchronized Data loadGlobal(int addr){
        return globals[addr];
    }

    public synchronized Heap getHeap() {
        return heap;
    }

    public synchronized Set<Reference> getRootPointers() {
        return roots;
    }

    protected synchronized void gc(TThread caller){
        gc(caller, null, null);
    }

    protected synchronized void gc(TThread caller, Reference prevPtr, Reference assignPtr) {
        gc.onAction(caller.getThreadID(), heap, assignPtr, prevPtr, roots);
    }

    protected void debug(TThread caller){
        debugger.onBreakPoint(this, caller);
    }

    public synchronized JIT getJit() {
        return jit;
    }
}
