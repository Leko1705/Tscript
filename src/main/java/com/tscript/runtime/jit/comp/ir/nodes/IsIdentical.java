package com.tscript.runtime.jit.comp.ir.nodes;

import com.tscript.runtime.jit.comp.ir.*;

/**
 * Checks whether a value is a specific runtime object (e.g. the class whose constructor was inlined).
 */
public class IsIdentical extends UnaryOp {

    /** ObjectCache id of the object */
    public final int objectId;

    public IsIdentical(IRValue value, int objectId) {
        super(value);
        this.objectId = objectId;
    }

    @Override
    protected boolean dataEquals(IRValue other) {
        return objectId == ((IsIdentical) other).objectId;
    }

    @Override
    protected int dataHashCode() {
        return objectId;
    }

    @Override
    public <P, R> R accept(IRValueVisitor<P, R> visitor, P p) {
        return visitor.visitIsIdentical(this, p);
    }
}
