package com.tscript.runtime.core;

import com.tscript.runtime.debug.*;
import com.tscript.runtime.heap.GenerationalHeap;
import com.tscript.runtime.heap.Heap;
import com.tscript.runtime.heap.gc.GarbageCollector;
import com.tscript.runtime.heap.gc.ReferenceCounting;
import com.tscript.runtime.heap.gc.SerialMSGC;
import com.tscript.runtime.jit.JIT;
import com.tscript.runtime.type.Callable;
import com.tscript.runtime.type.Member;
import com.tscript.runtime.type.TType;

import java.io.File;
import java.io.OutputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class TscriptVM implements Debuggable<VMInfo> {

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
    private final Map<Integer, TThread> threads = new ConcurrentHashMap<>();


    protected TscriptVM(OutputStream out, OutputStream err, Heap heap, GarbageCollector gc, Debugger debugger){
        this.out = out;
        this.err = err;
        this.heap = heap;
        this.gc = gc;
        this.debugger = Objects.requireNonNullElse(debugger, Debugger.getVoidDebugger());
        this.jit = new JIT(heap);
    }



    private int execute(File file){
        FileLoader fileLoader = new FileLoader(file, this, null);
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

    public void startNewThread(Callable callable){
        int nextID = freeThreadIDQueue.isEmpty()
                ? threads.size()
                : freeThreadIDQueue.poll();

        TThread thread = new TThread(this, callable, nextID);
        threads.put(nextID, thread);
        thread.start();
    }

    public void killThread(int id){
        TThread thread = threads.remove(id);
        if (thread != null)
            thread.terminate();
    }

    public Data storeGlobal(int addr, Data data){
        Data displaced = globals[addr];
        globals[addr] = data;
        return displaced;
    }

    public Data loadGlobal(int addr){
        return globals[addr];
    }

    public Heap getHeap() {
        return heap;
    }

    protected void gc(TThread caller){
        gc(caller, null, null);
    }

    protected void gc(TThread caller, Reference prevPtr, Reference assignPtr) {
        switch (gc.getType()){
            case TRACING -> evalTracingGC(caller);
            case COUNTING -> evalCountingGC(caller, prevPtr, assignPtr);
            default -> throw new IllegalStateException("unexpected GCType " + gc.getType());
        }
    }

    private void evalTracingGC(TThread caller){
        Collection<Reference> roots = findRoots();
        gc.onAction(caller.getThreadID(), heap, null, null, roots);
    }

    private void evalCountingGC(TThread caller, Reference prevPtr, Reference assignPtr){
        gc.onAction(caller.getThreadID(), heap, prevPtr, assignPtr, null);
    }

    private Collection<Reference> findRoots(){
        // since the tscript tends to have
        // many references we increase the
        // initialCapacity
        Collection<Reference> roots = new ArrayList<>(20);

        // collect globals
        for (Data data : globals)
            if (data != null && data.isReference())
                roots.add(data.asReference());

        // collect locals and current stack operands
        for (TThread thread : threads.values()){
            for (Frame frame : thread.frameStack) {
                for (Data data : frame.stack) {
                    if (data == null) break;
                    if (data.isReference())
                        roots.add(data.asReference());
                }
                for (Data data : frame.locals) {
                    if (data != null && data.isReference())
                        roots.add(data.asReference());
                }
            }
        }

        // collect statics
        FileManager manager = FileManager.getManager();
        for (Collection<TType> tTypes : manager.getTypes().values())
            for (TType type : tTypes)
                for (Member member : type.getMembers())
                    if (member.data != null && member.data.isReference())
                        roots.add(member.data.asReference());

        return roots;
    }

    protected DebugAction debug(TThread caller){
        return debugger.onBreakPoint(caller.getThreadID(), loadInfo(heap));
    }

    public JIT getJit() {
        return jit;
    }


    public void quit() {
        for (TThread thread : threads.values())
            thread.terminate();
        threads.clear();
    }

    @Override
    public VMInfo loadInfo(Heap heap) {
        return new VMInfoImpl();
    }

    private class VMInfoImpl implements VMInfo {

        private final List<ThreadInfo> threadTrees;

        private VMInfoImpl() {
            threadTrees = new ArrayList<>();
            for (TThread thread : threads.values())
                threadTrees.add(thread.loadInfo(heap));
        }

        @Override
        public List<ThreadInfo> getThreadTrees() {
            return threadTrees;
        }

        @Override
        public HeapInfo getHeapTree() {
            return heap.loadInfo(heap);
        }
    }

}
