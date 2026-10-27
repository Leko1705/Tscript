package com.tscript.runtime.jit.comp.ir.nodes;

import com.tscript.runtime.jit.comp.ir.*;

/**
 * Allocates an iterator over the integer range [from, to). Replaces GetIterator(Range(from, to)),
 * so that the range object itself does not have to be allocated.
 */
public class NewRangeIterator extends BinaryOp {

    public NewRangeIterator(IRValue from, IRValue to) {
        super(from, to);
    }

    @Override
    public boolean isFixed() {
        return true;
    }

    @Override
    public <P, R> R accept(IRValueVisitor<P, R> visitor, P p) {
        return visitor.visitNewRangeIterator(this, p);
    }
}
