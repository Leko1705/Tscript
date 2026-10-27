package com.tscript.runtime.jit.comp.ir.nodes;

import com.tscript.runtime.jit.comp.ir.*;
import com.tscript.runtime.jit.comp.optim.CanonicalizerTool;

/**
 * Creates an iterator for an iterable object.
 */
public class GetIterator extends UnaryOp {

    public GetIterator(IRValue value) {
        super(value);
    }

    @Override
    public boolean isFixed() {
        return true;
    }

    @Override
    public <P, R> R accept(IRValueVisitor<P, R> visitor, P p) {
        return visitor.visitGetIterator(this, p);
    }
}
