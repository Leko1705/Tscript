package com.tscript.runtime.jit.comp.optim;

import com.tscript.runtime.jit.comp.ir.Graph;
import com.tscript.runtime.jit.comp.ir.IRType;
import com.tscript.runtime.jit.comp.ir.IRValue;
import com.tscript.runtime.jit.comp.ir.nodes.Bool;
import com.tscript.runtime.jit.comp.ir.nodes.Int;
import com.tscript.runtime.jit.comp.ir.nodes.Real;
import com.tscript.runtime.jit.comp.ir.types.BoolType;
import com.tscript.runtime.jit.comp.ir.types.IntType;
import com.tscript.runtime.jit.comp.ir.types.RealType;

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

    public boolean isIntegerOf(IRValue value, int i) {
        return value instanceof Int ival && ival.value == i;
    }

    public boolean isRealOf(IRValue value, double d) {
        return value instanceof Real rval && rval.value == d;
    }

    public boolean isBooleanOf(IRValue value, boolean b) {
        return value instanceof Bool bval && bval.value == b;
    }

    public boolean isNumeric(IRValue value) {
        IRType ty = type(value);
        return ty == IntType.INSTANCE || ty == RealType.INSTANCE;
    }

    public boolean isInt(IRValue value) {
        return type(value) == IntType.INSTANCE;
    }

    public boolean isBool(IRValue value) {
        return type(value) == BoolType.INSTANCE;
    }

}
