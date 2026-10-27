package com.tscript.runtime.jit.comp.ir.nodes;

import com.tscript.runtime.jit.comp.ir.*;
import com.tscript.runtime.jit.comp.ir.*;

public class PopFrame extends IREffect {


    public PopFrame(IREffect previous, IRControl control, Graph graph) {
        super(previous, control, graph);
    }

    @Override
    public <P, R> R accept(IREffectVisitor<P, R> visitor, P p) {
        return visitor.visitPopFrame(this, p);
    }

}
