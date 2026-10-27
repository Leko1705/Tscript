package com.tscript.runtime.jit.comp.ir.stamps;

public interface Stamp {

    boolean isCompatible(Stamp other);

    Stamp union(Stamp other);

    Stamp intersection(Stamp other);

}
