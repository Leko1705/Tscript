package com.tscript.runtime.jit.comp.ir.nodes;

import com.tscript.runtime.jit.comp.ir.*;
import com.tscript.runtime.jit.comp.ir.*;

public class StartEffect extends IREffect {

    public StartEffect(IRControl control, Graph graph) {
        super(null, control, graph);
    }

    @Override
    public <P, R> R accept(IREffectVisitor<P, R> visitor, P p) {
        return visitor.visitStart(this, p);
    }

}
