package com.tscript.runtime.jit.ir.nodes;

import com.tscript.runtime.jit.ir.IRNode;

public abstract class Literal<T> implements IRNode {

    public T value;

    public Literal(T value) {
        this.value = value;
    }

}
