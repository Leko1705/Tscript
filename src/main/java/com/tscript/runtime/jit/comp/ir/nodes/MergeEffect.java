package com.tscript.runtime.jit.comp.ir.nodes;

import com.tscript.runtime.jit.comp.ir.*;
import com.tscript.runtime.jit.comp.ir.*;

import java.util.ArrayList;
import java.util.List;

public class MergeEffect extends IREffect {

    public final List<IREffect> previousEffects;

    public MergeEffect(IRControl control, Graph graph, IREffect... previousEffects) {
        super(null, control, graph);
        this.previousEffects = new ArrayList<>(List.of(previousEffects));
    }

    @Override
    public <P, R> R accept(IREffectVisitor<P, R> visitor, P p) {
        return visitor.visitMergeEffect(this, p);
    }

}
