package com.tscript.runtime.jit.comp.ir.nodes;

import com.tscript.runtime.jit.comp.ir.*;

/**
 * Loads a global variable. Fixed, since globals are mutable state.
 */
public class Global extends IRValue {

    public final GlobalRef global;

    public Global(GlobalRef global) {
        this.global = global;
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
        return name() + "#" + id + "(" + global.moduleId() + ":" + global.index() + ")";
    }
}
