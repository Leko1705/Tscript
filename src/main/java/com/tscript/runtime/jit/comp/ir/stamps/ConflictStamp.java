package com.tscript.runtime.jit.comp.ir.stamps;

public enum ConflictStamp implements Stamp {
    INSTANCE;

    @Override
    public boolean isCompatible(Stamp other) {
        return false;
    }

    @Override
    public Stamp union(Stamp other) {
        // Union with Illegal is just the other stamp
        // (Conflict | x) = Conflict
        return INSTANCE;
    }

    @Override
    public Stamp intersection(Stamp other) {
        // Intersection with Illegal is always Illegal
        // (Conflict & x) = x
        return other;
    }
}
