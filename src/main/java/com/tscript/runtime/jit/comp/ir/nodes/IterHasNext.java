package com.tscript.runtime.jit.comp.ir.nodes;

import com.tscript.runtime.jit.comp.ir.*;
import com.tscript.runtime.jit.comp.optim.CanonicalizerTool;

/**
 * Checks whether an iterator has a next element.
 */
public class IterHasNext extends UnaryOp {

    public IterHasNext(IRValue value) {
        super(value);
    }

    @Override
    public boolean isFixed() {
        return true;
    }

    @Override
    public <P, R> R accept(IRValueVisitor<P, R> visitor, P p) {
        return visitor.visitIterHasNext(this, p);
    }
}
