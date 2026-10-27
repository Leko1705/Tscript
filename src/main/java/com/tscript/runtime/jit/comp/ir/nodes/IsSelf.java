package com.tscript.runtime.jit.comp.ir.nodes;

import com.tscript.runtime.jit.comp.ir.*;

/**
 * Checks whether a value is the function that is currently executed by the compiled code
 * (same function metadata and same owner). Used to guard self tail calls that were turned into loops.
 */
public class IsSelf extends UnaryOp {

    public IsSelf(IRValue value) {
        super(value);
    }

    @Override
    public <P, R> R accept(IRValueVisitor<P, R> visitor, P p) {
        return visitor.visitIsSelf(this, p);
    }
}
