package com.tscript.runtime.jit.comp.ir.nodes;

import com.tscript.runtime.jit.comp.ir.*;
import com.tscript.runtime.jit.comp.ir.*;

import java.util.List;

public class NewLine extends IREffect {

    private final int line;

    public NewLine(int line, IREffect previous, IRControl control, Graph graph) {
        super(previous, control, graph);
        this.line = line;
    }

    public int getLine() {
        return line;
    }

    @Override
    public <P, R> R accept(IREffectVisitor<P, R> visitor, P p) {
        return visitor.visitNewLine(this, p);
    }

}
