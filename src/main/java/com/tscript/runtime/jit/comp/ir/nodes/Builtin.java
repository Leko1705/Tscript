package com.tscript.runtime.jit.comp.ir.nodes;

import com.tscript.runtime.jit.comp.ir.*;
import com.tscript.runtime.jit.comp.optim.CanonicalizerTool;

/**
 * Represents a builtin object (see Builtins#load).
 */
public class Builtin extends IRValue {

    public final int index;

    public Builtin(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    @Override
    public boolean isConstant() {
        return true;
    }

    @Override
    protected boolean dataEquals(IRValue other) {
        return index == ((Builtin) other).index;
    }

    @Override
    protected int dataHashCode() {
        return index;
    }

    @Override
    public <P, R> R accept(IRValueVisitor<P, R> visitor, P p) {
        return visitor.visitBuiltin(this, p);
    }

    @Override
    public String toString() {
        return name() + "#" + id + "(" + index + ")";
    }
}
