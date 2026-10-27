package com.tscript.runtime.jit.comp.optim;

import com.tscript.runtime.jit.comp.ir.Graph;
import com.tscript.runtime.jit.comp.ir.IRType;
import com.tscript.runtime.jit.comp.ir.IRValue;
import com.tscript.runtime.jit.comp.ir.nodes.*;
import com.tscript.runtime.jit.comp.ir.types.*;

/**
 * The context handed to {@link IRValue#canonicalize(CanonicalizerTool)}. It gives access to the
 * inferred types of values and to the graph builder, which must be used to create replacement
 * nodes (so that they are canonicalized and value numbered themselves).
 */
public class CanonicalizerTool {

    private final Graph graph;

    public CanonicalizerTool(Graph graph) {
        this.graph = graph;
    }

    public IRType type(IRValue value) {
        return graph.typeMap().getType(value);
    }

    public Graph.Builder builder() {
        return graph.builder();
    }

    /* ---------------- literal checks ---------------- */

    public boolean isIntegerOf(IRValue value, int i) {
        return value instanceof Int ival && ival.value == i;
    }

    public boolean isRealOf(IRValue value, double d) {
        return value instanceof Real rval && Double.compare(rval.value, d) == 0;
    }

    public boolean isBooleanOf(IRValue value, boolean b) {
        return value instanceof Bool bval && bval.value == b;
    }

    public boolean isStringOf(IRValue value, String s) {
        return value instanceof Str sval && sval.value.equals(s);
    }

    public boolean isNumericLiteral(IRValue value) {
        return value instanceof Int || value instanceof Real;
    }

    /**
     * @return the value of an integer or real literal as double
     */
    public double asDouble(IRValue value) {
        if (value instanceof Int i) return i.value;
        if (value instanceof Real r) return r.value;
        throw new IllegalArgumentException("not a numeric literal: " + value);
    }

    /* ---------------- type checks ---------------- */

    public boolean isInt(IRValue value) {
        return type(value) == IntType.INSTANCE;
    }

    public boolean isReal(IRValue value) {
        return type(value) == RealType.INSTANCE;
    }

    public boolean isNumeric(IRValue value) {
        IRType ty = type(value);
        return ty == IntType.INSTANCE || ty == RealType.INSTANCE;
    }

    public boolean isBool(IRValue value) {
        return type(value) == BoolType.INSTANCE;
    }

    public boolean isStr(IRValue value) {
        return type(value) == StrType.INSTANCE;
    }

    public boolean isPrimitive(IRType type) {
        return IRType.isPrimitive(type);
    }

    public com.tscript.runtime.typing.Type runtimeTypeOf(IRType type) {
        return IRType.runtimeTypeOf(type);
    }

}
