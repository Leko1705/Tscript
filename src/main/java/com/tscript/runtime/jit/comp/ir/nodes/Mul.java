package com.tscript.runtime.jit.comp.ir.nodes;

import com.tscript.runtime.jit.comp.ir.IRValue;
import com.tscript.runtime.jit.comp.ir.IRValueVisitor;
import com.tscript.runtime.jit.comp.ir.stamps.ConflictStamp;
import com.tscript.runtime.jit.comp.ir.stamps.IntegerStamp;
import com.tscript.runtime.jit.comp.ir.stamps.Stamp;
import com.tscript.runtime.jit.comp.optim.CanonicalizerTool;

public class Mul extends BinaryOp {

    public Mul(IRValue left, IRValue right) {
        super(left, right);
        stamp = computeInitialStamp(left, right);
    }

    @Override
    public <P, R> R accept(IRValueVisitor<P, R> visitor, P p) {
        return visitor.visitMul(this, p);
    }

    @Override
    public String toString() {
        return "(" + getLeft().toString() + " * " + getRight().toString() + ")";
    }

    @Override
    public IRValue canonicalize(CanonicalizerTool tool) {
        IRValue lhs = getLeft();
        IRValue rhs = getRight();

        if (lhs instanceof Int s && rhs instanceof Int l) {
            return tool.builder().integer(s.value * l.value);
        }
        if (lhs instanceof Int s && rhs instanceof Real l) {
            return tool.builder().real(l.value * s.value);
        }
        if (lhs instanceof Real s && rhs instanceof Int l) {
            return tool.builder().real(s.value * l.value);
        }
        if (lhs instanceof Real s && rhs instanceof Real l) {
            return tool.builder().real(l.value * s.value);
        }

        if (!lhs.isConstant() && rhs.isConstant()) {
            return tool.builder().mul(rhs, lhs);
        }

        if (tool.isRealOf(lhs, 0.0) || tool.isRealOf(rhs, 0.0)) {
            return tool.builder().real(0.0);
        }
        if (tool.isIntegerOf(lhs, 0) || tool.isIntegerOf(rhs, 0)) {
            return tool.builder().integer(0);
        }

        if (tool.isIntegerOf(lhs, 1) && tool.isNumeric(rhs)) {
            return rhs;
        }
        if (tool.isIntegerOf(rhs, 1) && tool.isNumeric(lhs)) {
            return lhs;
        }
        if (tool.isRealOf(lhs, 1.0) && tool.isNumeric(rhs)) {
            return rhs;
        }
        if (tool.isRealOf(rhs, 1.0) && tool.isNumeric(lhs)) {
            return lhs;
        }

        return this;
    }

    private Stamp computeInitialStamp(IRValue l, IRValue r) {
        if (l.stamp instanceof IntegerStamp i1 && r.stamp instanceof IntegerStamp i2) {
            // Multiplication example
            long a = (long)i1.min * i2.min;
            long b = (long)i1.min * i2.max;
            long c = (long)i1.max * i2.min;
            long d = (long)i1.max * i2.max;

            long min = Math.min(Math.min(a, b), Math.min(c, d));
            long max = Math.max(Math.max(a, b), Math.max(c, d));

            if (min >= Integer.MIN_VALUE && max <= Integer.MAX_VALUE)
                return new IntegerStamp((int)min, (int)max);
        }
        return ConflictStamp.INSTANCE;
    }
}