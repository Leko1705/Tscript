package com.tscript.runtime.jit.comp.ir.nodes;

import com.tscript.runtime.jit.comp.ir.*;

/**
 * Performs container[key] = value.
 */
public class ContainerWrite extends IREffect {

    public final int line;

    public ContainerWrite(IRValue container, IRValue key, IRValue value, IRControl control, int line) {
        super(control, container, key, value);
        this.line = line;
    }

    public IRValue getContainer() {
        return input(0);
    }

    public IRValue getKey() {
        return input(1);
    }

    public IRValue getValue() {
        return input(2);
    }

    @Override
    public <P, R> R accept(IREffectVisitor<P, R> visitor, P p) {
        return visitor.visitContainerWrite(this, p);
    }
}
