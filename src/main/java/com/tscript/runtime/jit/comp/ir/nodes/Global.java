package com.tscript.runtime.jit.comp.ir.nodes;

import com.tscript.runtime.jit.comp.ir.*;
import com.tscript.runtime.jit.comp.optim.CanonicalizerTool;

/**
 * Loads a global of the module of the traced function. Fixed, since globals are mutable state.
 */
public class Global extends IRValue {

    public final int index;

    public Global(int index) {
        this.index = index;
    }

    @Override
    public boolean isFixed() {
        return true;
    }

    @Override
    public <P, R> R accept(IRValueVisitor<P, R> visitor, P p) {
        return visitor.visitGlobal(this, p);
    }

    @Override
    public String toString() {
        return name() + "#" + id + "(" + index + ")";
    }
}
