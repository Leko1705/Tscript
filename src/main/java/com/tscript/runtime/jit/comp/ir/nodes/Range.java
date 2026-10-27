package com.tscript.runtime.jit.comp.ir.nodes;

import com.tscript.runtime.jit.comp.ir.*;
import com.tscript.runtime.jit.comp.optim.CanonicalizerTool;

/**
 * Creates an (immutable) range object.
 */
public class Range extends BinaryOp {

    public Range(IRValue from, IRValue to) {
        super(from, to);
    }

    public IRValue getFrom() {
        return getLeft();
    }

    public IRValue getTo() {
        return getRight();
    }

    @Override
    public <P, R> R accept(IRValueVisitor<P, R> visitor, P p) {
        return visitor.visitRange(this, p);
    }
}
