package com.tscript.runtime.jit.comp.ir.stamps;

public enum IllegalStamp implements Stamp {
    INSTANCE;

    @Override
    public boolean isCompatible(Stamp other) {
        return false;
    }

    @Override
    public Stamp union(Stamp other) {
        // Union with Illegal is just the other stamp
        // (Impossibility | x) = x
        return other;
    }

    @Override
    public Stamp intersection(Stamp other) {
        // Intersection with Illegal is always Illegal
        // (Impossibility & x) = Impossibility
        return INSTANCE;
    }
}
