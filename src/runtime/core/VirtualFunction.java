package runtime.core;

import runtime.jit.JIT;
import runtime.jit.JITSensitive;
import runtime.jit.OptimizeVirtualTask;
import runtime.type.Callable;

import java.util.LinkedHashMap;
import java.util.List;

public class VirtualFunction extends Callable {

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

    public int getLocals() {
        return locals;
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
        Callable optimized = jit.getOptimized(this);
        if (optimized != null){
            optimized.setOwner(getOwner());
            caller.putFrame(optimized);
            Data d = optimized.eval(caller, params);
            caller.popFrame();
            return d;
        }
        else {
            hotness++;
            if (hotness >= 100_000)
                jit.addTask(new OptimizeVirtualTask(this));
        }
        
        caller.invoke(this);
        Data[] data = params.values().toArray(new Data[0]);
        for (int i = data.length-1; i >= 0; i--)
            caller.push(data[i]);
        return null;
    }

    public byte[][] getInstructions() {
        return instructions;
    }

    @JITSensitive
    public Pool getPool() {
        return pool;
    }
}
