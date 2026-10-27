package com.tscript.runtime.jit.comp.ir.nodes;

import com.tscript.runtime.jit.comp.ir.*;
import com.tscript.runtime.jit.comp.optim.CanonicalizerTool;

/**
 * Checks whether a value is a virtual function with the given metadata. Guards inlined calls.
 */
public class IsCallee extends UnaryOp {

    /** ObjectCache id of the function metadata */
    public final int metadataId;

    public IsCallee(IRValue value, int metadataId) {
        super(value);
        this.metadataId = metadataId;
    }

    @Override
    public IRValue canonicalize(CanonicalizerTool tool) {
        if (getValue() instanceof NewFunction function) {
            return tool.builder().bool(function.metadataId == metadataId);
        }
        return this;
    }

    @Override
    protected boolean dataEquals(IRValue other) {
        return metadataId == ((IsCallee) other).metadataId;
    }

    @Override
    protected int dataHashCode() {
        return metadataId;
    }

    @Override
    public <P, R> R accept(IRValueVisitor<P, R> visitor, P p) {
        return visitor.visitIsCallee(this, p);
    }
}
