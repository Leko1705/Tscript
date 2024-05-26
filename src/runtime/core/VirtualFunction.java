package runtime.core;

import runtime.jit.compile.JITCompiler;
import runtime.jit.compile.MethodTask;
import runtime.type.Callable;

import java.util.LinkedHashMap;

public class VirtualFunction extends Callable {

    private static final int JIT_HOTNESS_THRESHOLD = 100;

    private final String name;
    private final LinkedHashMap<String, Data> params;
    private final byte[][] instructions;
    private final int stackSize;
    private final int locals;
    private final Pool pool;

    private int hotness = 0;

    public VirtualFunction(String name,
                           byte[][] instructions,
                           int stackSize,
                           int locals,
                           LinkedHashMap<String, Data> params,
                           Pool pool) {
        this.name = name;
        this.instructions = instructions;
        this.stackSize = stackSize;
        this.locals = locals;
        this.pool = pool;
        this.params = params;
    }

    public String getName() {
        return name;
    }

    public Frame buildFrame(){
        return new Frame(getOwner(), name, instructions, stackSize, locals, pool);
    }

    public Pool getPool() {
        return pool;
    }

    public int getStackSize() {
        return stackSize;
    }

    public int getLocals() {
        return locals;
    }

    @Override
    public LinkedHashMap<String, Data> getParameters() {
        return params;
    }

    @Override
    public Data eval(TThread caller, Data[] args) {
        if (isHot()) {
            return callAsHotSpot(caller, args);
        }
        hotness++;
        return callDefault(caller, args);
    }

    private Data callAsHotSpot(TThread caller, Data[] args){
        Callable optimized = getOptimized(caller, args);
        if (optimized == null) {
            releaseJITOptimization(caller, args);
            return callDefault(caller, args);
        }
        else {
            return callJITOptimized(caller, optimized, args);
        }
    }

    private Callable getOptimized(TThread caller, Data[] args){
        JITCompiler jit = caller.getJIT();
        Callable optimized = jit.getLookUpTable().getOptimized(this);
        if (optimized != null)
            optimized.setOwner(getOwner());
        return optimized;
    }

    private Data callDefault(TThread caller, Data[] args){
        caller.invoke(this);
        for (int i = args.length-1; i >= 0; i--)
            caller.push(args[i]);
        return null;
    }

    private Data callJITOptimized(TThread caller,
                                  Callable optimized,
                                  Data[] params){
        //caller.putFrame(optimized, pool);
        Data d = optimized.eval(caller, params);
        //caller.popFrame();
        return d;
    }

    private void releaseJITOptimization(TThread caller, Data[] args){
        JITCompiler jit = caller.getJIT();
        jit.handle(new MethodTask(this, args, caller));
    }

    public byte[][] getByteCode() {
        return instructions;
    }

    public boolean isHot(){
        return hotness >= JIT_HOTNESS_THRESHOLD;
    }
}
