package com.tscript.runtime.jit.comp.ir.nodes;

import com.tscript.runtime.jit.comp.ir.*;
import com.tscript.runtime.jit.comp.optim.CanonicalizerTool;

/**
 * Represents the value of a local variable of the interpreter frame at trace entry.
 */
public class Local extends IRValue {

    public final int index;

    /** the type of the value if it is statically known (entries of side traces), unknown otherwise */
    public final IRType type;

    public Local(int index) {
        this(index, com.tscript.runtime.jit.comp.ir.types.UnknownType.INSTANCE);
    }

    public Local(int index, IRType type) {
        this.index = index;
        this.type = type;
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
        return index == ((Local) other).index && type.equals(((Local) other).type);
    }

    @Override
    protected int dataHashCode() {
        return index;
    }

    @Override
    public <P, R> R accept(IRValueVisitor<P, R> visitor, P p) {
        return visitor.visitLocal(this, p);
    }

    @Override
    public String toString() {
        return name() + "#" + id + "(" + index + ")";
    }
}
