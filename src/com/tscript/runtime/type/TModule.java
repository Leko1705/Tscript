package com.tscript.runtime.type;

import com.tscript.runtime.core.Data;
import com.tscript.runtime.core.Pool;
import com.tscript.runtime.core.VirtualFunction;

import java.util.List;

public class TModule extends BaseObject {

    private static final TType TYPE = new TType("Module", null);

    private final Pool pool;
    private final int entryPoint;
    private final Data[] globals;

    public TModule(List<Member> members, Pool pool, int entryPoint, int globals) {
        super(members);
        this.pool = pool;
        this.entryPoint = entryPoint;
        this.globals = new Data[globals];
    }

    @Override
    public TType getType() {
        return TYPE;
    }

    public Pool getPool() {
        return pool;
    }

    public Data storeGlobal(int index, Data data){
        Data prev = globals[index];
        globals[index] = data;
        return prev;
    }

    public Data loadGlobal(int index){
        return globals[index];
    }

    public VirtualFunction getEntryFunction(){
        return (VirtualFunction) pool.loadUnsafe(entryPoint);
    }

    public Data[] getGlobals() {
        return globals;
    }
}
