package com.tscript.runtime.jit.ir.nodes;

public abstract class Literal<T> implements IRValue {

    public T value;

    public Literal(T value) {
        this.value = value;
    }

}
