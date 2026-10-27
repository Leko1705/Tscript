package com.tscript.runtime.jit.comp.ir.nodes;

import com.tscript.runtime.jit.comp.ir.*;
import com.tscript.runtime.jit.comp.optim.CanonicalizerTool;

/**
 * The value of a function argument at trace entry. Images of function traces are specialized
 * on the runtime types of their arguments, therefore every parameter carries a known type.
 */
public class Parameter extends IRValue {

    public final int index;
    public final IRType type;

    public Parameter(int index, IRType type) {
        this.index = index;
        this.type = type;
    }

    @Override
    protected boolean dataEquals(IRValue other) {
        return index == ((Parameter) other).index;
    }

    @Override
    protected int dataHashCode() {
        return index;
    }

    @Override
    public <P, R> R accept(IRValueVisitor<P, R> visitor, P p) {
        return visitor.visitParameter(this, p);
    }

    @Override
    public String toString() {
        return name() + "#" + id + "(" + index + ": " + type + ")";
    }
}
