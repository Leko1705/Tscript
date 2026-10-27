package com.tscript.runtime.jit.comp.ir.stamps;

public class IntegerStamp implements Stamp {

    public static final IntegerStamp ANY = new IntegerStamp(Integer.MIN_VALUE, Integer.MAX_VALUE);

    public final int max, min;

    public IntegerStamp(int max, int min) {
        this.max = max;
        this.min = min;
    }

    @Override
    public boolean isCompatible(Stamp other) {
        return other instanceof IntegerStamp;
    }

    @Override
    public Stamp union(Stamp other) {
        if (other instanceof IntegerStamp o) {
            return new IntegerStamp(
                    Math.min(this.min, o.min),
                    Math.max(this.max, o.max)
            );
        }
        return VoidStamp.INSTANCE;
    }

    @Override
    public Stamp intersection(Stamp other) {
        if (other instanceof IntegerStamp o) {
            // "Join": The result is the overlap between the two.
            // Used at Guards (Narrowing).
            int newMin = Math.max(this.min, o.min);
            int newMax = Math.min(this.max, o.max);

            if (newMin > newMax) return IllegalStamp.INSTANCE; // No possible value exists
            return new IntegerStamp(newMin, newMax);
        }
        return IllegalStamp.INSTANCE;
    }

    public boolean isPositive() {
        return min >= 0;
    }

    public boolean isNegative() {
        return max < 0;
    }

    public boolean isSingleValue() {
        return min == max;
    }
}
