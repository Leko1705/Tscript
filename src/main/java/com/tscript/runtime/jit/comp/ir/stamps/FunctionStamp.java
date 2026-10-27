package com.tscript.runtime.jit.comp.ir.stamps;

public class FunctionStamp implements Stamp {


    public static final FunctionStamp ANY = new FunctionStamp(-1, true);


    public final int identity;
    public final boolean isAny;

    public FunctionStamp(int identity, boolean isAny) {
        this.identity = identity;
        this.isAny = isAny;
    }

    public FunctionStamp(int identity) {
        this(identity, false);
    }

    @Override
    public boolean isCompatible(Stamp other) {
        return other instanceof FunctionStamp;
    }

    @Override
    public Stamp union(Stamp other) {
        if (other instanceof FunctionStamp o) {
            if (this.isAny || o.isAny) return ANY;
            if (this.identity == o.identity) return this;
            // Two different functions meet at a Merge: we know it's a function,
            // but we no longer know which one.
            return ANY;
        }
        return ConflictStamp.INSTANCE;
    }

    @Override
    public Stamp intersection(Stamp other) {
        if (other instanceof FunctionStamp o) {
            if (this.isAny) return o;
            if (o.isAny) return this;
            if (this.identity == o.identity) return this;
            // Trying to be Function A AND Function B at the same time
            return IllegalStamp.INSTANCE;
        }
        return IllegalStamp.INSTANCE;
    }

    public boolean isKnown() {
        return !isAny;
    }
}
