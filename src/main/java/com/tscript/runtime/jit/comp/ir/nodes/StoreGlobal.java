package com.tscript.runtime.jit.comp.ir.nodes;

import com.tscript.runtime.jit.comp.ir.*;

/**
 * Stores a value into a global of the module of the traced function.
 */
public class StoreGlobal extends IREffect {

    public final int index;

    public final int line;

    public StoreGlobal(int index, IRValue value, IRControl control, int line) {
        super(control, value);
        this.index = index;
        this.line = line;
    }

    public IRValue getValue() {
        return input(0);
    }

    @Override
    public <P, R> R accept(IREffectVisitor<P, R> visitor, P p) {
        return visitor.visitStoreGlobal(this, p);
    }
}
