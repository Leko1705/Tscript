package com.tscript.runtime.jit.comp.ir.nodes;

import com.tscript.runtime.jit.comp.ir.*;

/**
 * Pins a fixed value node to its position in the effect chain. The value is evaluated exactly
 * at this point.
 */
public class Anchor extends IREffect {

    public final int line;

    public Anchor(IRValue value, IRControl control, int line) {
        super(control, value);
        this.line = line;
    }

    public IRValue getValue() {
        return input(0);
    }

    @Override
    public <P, R> R accept(IREffectVisitor<P, R> visitor, P p) {
        return visitor.visitAnchor(this, p);
    }
}
