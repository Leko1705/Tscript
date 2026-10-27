package com.tscript.runtime.jit.comp.ir.nodes;

import com.tscript.runtime.jit.comp.ir.*;

/**
 * Pins a fixed value node to its position in the effect chain. The value is evaluated exactly
 * at this point.
 */
public class Anchor extends IREffect {

    /** ObjectCache id of the {@link com.tscript.runtime.jit.SourceSite} of this position */
    public final int site;

    public Anchor(IRValue value, IRControl control, int site) {
        super(control, value);
        this.site = site;
    }

    public IRValue getValue() {
        return input(0);
    }

    @Override
    public <P, R> R accept(IREffectVisitor<P, R> visitor, P p) {
        return visitor.visitAnchor(this, p);
    }
}
