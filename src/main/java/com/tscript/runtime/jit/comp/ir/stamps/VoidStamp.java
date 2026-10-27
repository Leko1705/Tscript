package com.tscript.runtime.jit.comp.ir.stamps;

public enum VoidStamp implements Stamp {
    INSTANCE;

    @Override
    public boolean isCompatible(Stamp other) {
        return other == INSTANCE;
    }

    @Override
    public Stamp union(Stamp other) {
        return (other == INSTANCE) ? INSTANCE : IllegalStamp.INSTANCE;
    }

    @Override
    public Stamp intersection(Stamp other) {
        return (other == INSTANCE) ? INSTANCE : IllegalStamp.INSTANCE;
    }
}