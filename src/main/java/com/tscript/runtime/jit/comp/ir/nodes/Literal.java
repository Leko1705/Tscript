package com.tscript.runtime.jit.comp.ir.nodes;

import com.tscript.runtime.jit.comp.ir.*;
import com.tscript.runtime.jit.comp.optim.CanonicalizerTool;

import java.util.Objects;

public abstract class Literal<T> extends IRValue {

    public final T value;

    protected Literal(T value) {
        this.value = value;
    }

    public T getValue() {
        return value;
    }

    @Override
    public boolean isConstant() {
        return true;
    }

    @Override
    protected boolean dataEquals(IRValue other) {
        return Objects.equals(value, ((Literal<?>) other).value);
    }

    @Override
    protected int dataHashCode() {
        return Objects.hashCode(value);
    }

    /**
     * @return the string representation used by the runtime (TNIUtils.toString) for this literal
     */
    public String displayString() {
        return Objects.toString(value);
    }

    @Override
    public String toString() {
        return name() + "#" + id + "(" + value + ")";
    }
}
