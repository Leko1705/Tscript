package com.tscript.runtime.jit.comp.ir.nodes;

import com.tscript.runtime.jit.comp.ir.*;
import com.tscript.runtime.jit.comp.optim.CanonicalizerTool;

/**
 * Represents the value of an operand stack slot of the interpreter frame at trace entry.
 */
public class StackValue extends IRValue {

    public final int index;

    public StackValue(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    @Override
    public boolean isConstant() {
        return false;
    }

    @Override
    protected boolean dataEquals(IRValue other) {
        return index == ((StackValue) other).index;
    }

    @Override
    protected int dataHashCode() {
        return index;
    }

    @Override
    public <P, R> R accept(IRValueVisitor<P, R> visitor, P p) {
        return visitor.visitStackValue(this, p);
    }

    @Override
    public String toString() {
        return name() + "#" + id + "(" + index + ")";
    }
}
