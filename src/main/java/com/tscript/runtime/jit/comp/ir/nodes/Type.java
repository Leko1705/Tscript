package com.tscript.runtime.jit.comp.ir.nodes;

import com.tscript.runtime.jit.comp.ir.*;
import com.tscript.runtime.jit.comp.optim.CanonicalizerTool;

/**
 * Represents a runtime type registered in the ObjectCache.
 */
public class Type extends IRValue {

    public final int index;

    public Type(int index) {
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
        return index == ((Type) other).index;
    }

    @Override
    protected int dataHashCode() {
        return index;
    }

    @Override
    public <P, R> R accept(IRValueVisitor<P, R> visitor, P p) {
        return visitor.visitType(this, p);
    }

    @Override
    public String toString() {
        return name() + "#" + id + "(" + index + ")";
    }
}
