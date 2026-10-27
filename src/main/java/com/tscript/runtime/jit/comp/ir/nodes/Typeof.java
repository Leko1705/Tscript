package com.tscript.runtime.jit.comp.ir.nodes;

import com.tscript.runtime.jit.comp.ir.*;
import com.tscript.runtime.jit.comp.optim.CanonicalizerTool;

import com.tscript.runtime.jit.comp.ir.types.*;

/**
 * Yields the runtime type object of a value.
 */
public class Typeof extends UnaryOp {

    public Typeof(IRValue value) {
        super(value);
    }

    @Override
    public <P, R> R accept(IRValueVisitor<P, R> visitor, P p) {
        return visitor.visitTypeof(this, p);
    }

    @Override
    public IRValue canonicalize(CanonicalizerTool tool) {
        IRType type = tool.type(getValue());
        com.tscript.runtime.typing.Type runtimeType = tool.runtimeTypeOf(type);
        if (runtimeType != null) {
            return tool.builder().type(runtimeType);
        }
        return this;
    }
}
