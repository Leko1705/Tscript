package com.tscript.runtime.jit.comp.ir.nodes;

import com.tscript.runtime.jit.comp.ir.*;

public class StartEffect extends IREffect {

    public StartEffect(IRControl control) {
        super(control);
    }

    @Override
    public <P, R> R accept(IREffectVisitor<P, R> visitor, P p) {
        return visitor.visitStart(this, p);
    }
}
