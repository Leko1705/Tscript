package com.tscript.runtime.jit.comp.ir.nodes;

import com.tscript.runtime.jit.comp.ir.*;

/**
 * Stores a value into a global of the module of the traced function.
 */
public class StoreGlobal extends IREffect {

    public final GlobalRef global;

    /** ObjectCache id of the {@link com.tscript.runtime.jit.SourceSite} of this store */
    public final int site;

    public StoreGlobal(GlobalRef global, IRValue value, IRControl control, int site) {
        super(control, value);
        this.global = global;
        this.site = site;
    }

    public IRValue getValue() {
        return input(0);
    }

    @Override
    public <P, R> R accept(IREffectVisitor<P, R> visitor, P p) {
        return visitor.visitStoreGlobal(this, p);
    }
}
