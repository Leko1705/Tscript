package com.tscript.runtime.jit.comp.ir.nodes;

import com.tscript.runtime.jit.comp.ir.IRValue;

import java.util.Objects;

public abstract class Literal<T> extends IRValue {

    public final T value;

    public Literal(T value) {
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
    public String toString() {
        return Objects.toString(value);
    }
}
