package com.tscript.runtime.core;

import com.tscript.runtime.jit.JIT;
import com.tscript.runtime.jit.JITSensitive;
import com.tscript.runtime.jit.OptimizeVirtualTask;
import com.tscript.runtime.type.Callable;

import java.util.LinkedHashMap;

public class VirtualFunction extends Callable {

    private final String name;
    private final LinkedHashMap<String, Data> params;
    private final byte[][] instructions;
    private final int stackSize;
    private final int locals;
    private final Pool pool;

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

    @Override
    public LinkedHashMap<String, Data> getParameters() {
        return params;
    }

    @Override
    public Data eval(TThread caller, LinkedHashMap<String, Data> params) {
        JIT jit = caller.getJIT();
        Data[] args = flatten(params);
        if (jit.isHot(name))
            return callAsHotSpot(caller, args);
        else
            return callDefault(caller, args);
    }

    private Data callAsHotSpot(TThread caller, Data[] args){
        Callable optimized = getOptimized(caller, args);
        if (optimized == null) {
            releaseJITOptimization(caller, args);
            return callDefault(caller, args);
        }
        else {
            return callJITOptimized(caller, optimized, params);
        }
    }

    private Callable getOptimized(TThread caller, Data[] args){
        JIT jit = caller.getJIT();
        Callable optimized = jit.getOptimized(this, args);
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
                                  LinkedHashMap<String, Data> params){
        caller.putFrame(optimized);
        Data d = optimized.eval(caller, params);
        caller.popFrame();
        return d;
    }

    private static Data[] flatten(LinkedHashMap<String, Data> params){
        return params.values().toArray(new Data[0]);
    }

    private void releaseJITOptimization(TThread caller, Data[] args){
        JIT jit = caller.getJIT();
        jit.addTask(new OptimizeVirtualTask(this, args));
    }

    @JITSensitive
    public byte[][] getInstructions() {
        return instructions;
    }

    @JITSensitive
    public Pool getPool() {
        return pool;
    }
}
