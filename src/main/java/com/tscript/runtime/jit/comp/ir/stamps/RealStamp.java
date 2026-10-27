package com.tscript.runtime.jit.comp.ir.stamps;

public class RealStamp implements Stamp {

    public static final RealStamp ANY = new RealStamp(Integer.MIN_VALUE, Integer.MAX_VALUE);

    public final double max, min;

    public RealStamp(double max, double min) {
        this.max = max;
        this.min = min;
    }

    @Override
    public boolean isCompatible(Stamp other) {
        return other instanceof RealStamp;
    }

    @Override
    public Stamp union(Stamp other) {
        if (other instanceof RealStamp o) {
            return new RealStamp(
                    Math.min(this.min, o.min),
                    Math.max(this.max, o.max)
            );
        }
        return VoidStamp.INSTANCE;
    }

    @Override
    public Stamp intersection(Stamp other) {
        if (other instanceof RealStamp o) {
            // "Join": The result is the overlap between the two.
            // Used at Guards (Narrowing).
            double newMin = Math.max(this.min, o.min);
            double newMax = Math.min(this.max, o.max);

            if (newMin > newMax) return IllegalStamp.INSTANCE; // No possible value exists
            return new RealStamp(newMin, newMax);
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
