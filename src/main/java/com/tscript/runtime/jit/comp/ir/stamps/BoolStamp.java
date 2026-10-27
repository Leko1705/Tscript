package com.tscript.runtime.jit.comp.ir.stamps;

public class BoolStamp implements Stamp {

    public static final BoolStamp ALWAYS_TRUE = new BoolStamp(true, false);
    public static final BoolStamp ALWAYS_FALSE = new BoolStamp(false, true);
    public static final BoolStamp UNKNOWN = new BoolStamp(true, true);
    public static final BoolStamp ILLEGAL = new BoolStamp(false, false);


    public final boolean canBeTrue, canBeFalse;

    public BoolStamp(boolean canBeTrue, boolean canBeFalse) {
        this.canBeTrue = canBeTrue;
        this.canBeFalse = canBeFalse;
    }

    @Override
    public boolean isCompatible(Stamp other) {
        return other instanceof BoolStamp;
    }

    @Override
    public Stamp union(Stamp other) {
        if (other instanceof BoolStamp o) {
            return new BoolStamp(this.canBeTrue || o.canBeTrue, this.canBeFalse || o.canBeFalse);
        }
        return ConflictStamp.INSTANCE;
    }

    @Override
    public Stamp intersection(Stamp other) {
        if (other instanceof BoolStamp o) {
            return new BoolStamp(this.canBeTrue && o.canBeTrue, this.canBeFalse && o.canBeFalse);
        }
        return IllegalStamp.INSTANCE;
    }

    public boolean isConstant() {
        return canBeTrue != canBeFalse; // One is true, one is false
    }

    public boolean getConstantValue() {
        return canBeTrue; // Only call if isConstant() is true
    }
}