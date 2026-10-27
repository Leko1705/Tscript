package com.tscript.runtime.jit.comp.ir.nodes;

import com.tscript.runtime.jit.comp.ir.*;

/**
 * The owner ("this") of a function object. Read when an inlined call starts.
 */
public class FunctionOwner extends UnaryOp {

    public FunctionOwner(IRValue function) {
        super(function);
    }

    @Override
    public boolean isFixed() {
        return true;
    }

    @Override
    public <P, R> R accept(IRValueVisitor<P, R> visitor, P p) {
        return visitor.visitFunctionOwner(this, p);
    }
}
