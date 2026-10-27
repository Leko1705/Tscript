package com.tscript.runtime.jit.comp.ir.nodes;

import com.tscript.runtime.jit.comp.ir.*;
import com.tscript.runtime.jit.comp.optim.CanonicalizerTool;

/**
 * A value narrowed to a known type. A Pi node is anchored directly behind the guard that
 * established its type and must not be scheduled before that guard.
 */
public class Pi extends UnaryOp {

    public final IRType type;

    public Pi(IRValue value, IRType type) {
        super(value);
        this.type = type;
    }

    @Override
    public boolean isFixed() {
        return true;
    }

    @Override
    public <P, R> R accept(IRValueVisitor<P, R> visitor, P p) {
        return visitor.visitPi(this, p);
    }

    @Override
    public IRValue canonicalize(CanonicalizerTool tool) {
        IRValue value = getValue();
        if (tool.type(value).equals(type)) {
            return value;
        }
        return this;
    }

    @Override
    public String toString() {
        return name() + "#" + id + "(#" + getValue().id + " as " + type + ")";
    }
}
