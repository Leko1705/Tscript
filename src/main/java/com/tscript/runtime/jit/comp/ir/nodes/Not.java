package com.tscript.runtime.jit.comp.ir.nodes;

import com.tscript.runtime.jit.comp.ir.*;
import com.tscript.runtime.jit.comp.optim.CanonicalizerTool;

/**
 * Logical not on booleans, bitwise complement on integers.
 */
public class Not extends UnaryOp {

    public Not(IRValue value) {
        super(value);
    }

    @Override
    public <P, R> R accept(IRValueVisitor<P, R> visitor, P p) {
        return visitor.visitNot(this, p);
    }

    @Override
    public IRValue canonicalize(CanonicalizerTool tool) {
        IRValue value = getValue();

        if (value instanceof Int i) {
            return tool.builder().integer(~i.value);
        }
        if (value instanceof Bool b) {
            return tool.builder().bool(!b.value);
        }
        if (value instanceof Not n && (tool.isInt(n.getValue()) || tool.isBool(n.getValue()))) {
            return n.getValue();
        }
        return this;
    }
}
