package com.tscript.runtime.jit.comp.ir.nodes;

import com.tscript.runtime.jit.comp.ir.IRValue;
import com.tscript.runtime.jit.comp.ir.IRValueVisitor;
import com.tscript.runtime.jit.comp.ir.stamps.*;
import com.tscript.runtime.jit.comp.optim.CanonicalizerTool;

public class Add extends BinaryOp {

    public Add(IRValue left, IRValue right) {
        super(left, right);
        stamp = computeInitialStamp(left, right);
    }

    @Override
    public <P, R> R accept(IRValueVisitor<P, R> visitor, P p) {
        return visitor.visitAdd(this, p);
    }

    @Override
    public String toString() {
        return "(" + getLeft().toString() + " + " + getRight().toString() + ")";
    }

    @Override
    public IRValue canonicalize(CanonicalizerTool tool) {
        IRValue lhs = getLeft();
        IRValue rhs = getRight();

        if (lhs instanceof Str s && rhs instanceof Literal<?> l) {
            return tool.builder().string(s.value + l.value);
        }
        if (rhs instanceof Str s && lhs instanceof Literal<?> l) {
            return tool.builder().string(l.value + s.value);
        }
        if (lhs instanceof Int s && rhs instanceof Int l) {
            return tool.builder().integer(s.value + l.value);
        }
        if (lhs instanceof Int s && rhs instanceof Real l) {
            return tool.builder().real(l.value + s.value);
        }
        if (lhs instanceof Real s && rhs instanceof Int l) {
            return tool.builder().real(s.value + l.value);
        }
        if (lhs instanceof Real s && rhs instanceof Real l) {
            return tool.builder().real(l.value + s.value);
        }

        if (!lhs.isConstant() && rhs.isConstant()) {
            return tool.builder().add(rhs, lhs);
        }

        if (tool.isIntegerOf(lhs, 0) && tool.isNumeric(rhs)) {
            return rhs;
        }
        if (tool.isIntegerOf(rhs, 0) && tool.isNumeric(lhs)) {
            return lhs;
        }
        if (tool.isRealOf(lhs, 0.0) && tool.isNumeric(rhs)) {
            return rhs;
        }
        if (tool.isRealOf(rhs, 0.0) && tool.isNumeric(lhs)) {
            return lhs;
        }

        return this;
    }


    private Stamp computeInitialStamp(IRValue l, IRValue r) {
        Stamp s1 = l.stamp;
        Stamp s2 = r.stamp;

        if (s1 instanceof IntegerStamp i1 && s2 instanceof IntegerStamp i2) {
            long newMin = (long) i1.min + i2.min;
            long newMax = (long) i1.max + i2.max;

            // If the result fits in a 32-bit int, keep the tight bounds
            if (newMin >= Integer.MIN_VALUE && newMax <= Integer.MAX_VALUE) {
                return new IntegerStamp((int) newMin, (int) newMax);
            } else {
                // If it might overflow, we lose precision unless you handle wrap-around
                return IntegerStamp.ANY;
            }
        }

        if (s1 instanceof RealStamp i1 && s2 instanceof RealStamp i2) {
            double newMin = i1.min + i2.min;
            double newMax = i1.max + i2.max;
            return new RealStamp(newMin, newMax);
        }

        return ConflictStamp.INSTANCE;
    }
}
