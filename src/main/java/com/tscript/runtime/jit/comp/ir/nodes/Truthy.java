package com.tscript.runtime.jit.comp.ir.nodes;

import com.tscript.runtime.jit.comp.ir.*;
import com.tscript.runtime.jit.comp.optim.CanonicalizerTool;

import com.tscript.runtime.jit.comp.ir.types.*;

/**
 * The truthiness of a value as defined by TNIUtils.isTrue.
 */
public class Truthy extends UnaryOp {

    public Truthy(IRValue value) {
        super(value);
    }

    @Override
    public <P, R> R accept(IRValueVisitor<P, R> visitor, P p) {
        return visitor.visitTruthy(this, p);
    }

    @Override
    public IRValue canonicalize(CanonicalizerTool tool) {
        IRValue value = getValue();
        if (value instanceof Bool) return value;
        if (value instanceof Int i) return tool.builder().bool(i.value != 0);
        if (value instanceof Real) return tool.builder().bool(true);
        if (value instanceof Str s) return tool.builder().bool(!s.value.isEmpty());
        if (value instanceof NullValue) return tool.builder().bool(false);

        IRType type = tool.type(value);
        if (type == BoolType.INSTANCE) return value;
        if (type == NullType.INSTANCE) return tool.builder().bool(false);
        if (type == RealType.INSTANCE || type == RangeType.INSTANCE || type instanceof TypeMirror) {
            return tool.builder().bool(true);
        }
        return this;
    }
}
