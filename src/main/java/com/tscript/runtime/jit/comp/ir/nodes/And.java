package com.tscript.runtime.jit.comp.ir.nodes;

import com.tscript.runtime.jit.comp.ir.IRValue;
import com.tscript.runtime.jit.comp.ir.IRValueVisitor;
import com.tscript.runtime.jit.comp.ir.stamps.*;
import com.tscript.runtime.jit.comp.ir.types.BoolType;
import com.tscript.runtime.jit.comp.optim.CanonicalizerTool;

public class And extends BinaryOp {

    public And(IRValue left, IRValue right) {
        super(left, right);
        stamp = computeInitialStamp(left, right);
    }

    @Override
    public <P, R> R accept(IRValueVisitor<P, R> visitor, P p) {
        return visitor.visitAnd(this, p);
    }

    @Override
    public String toString() {
        return "(" + getLeft().toString() + " and " + getRight().toString() + ")";
    }

    @Override
    public IRValue canonicalize(CanonicalizerTool tool) {
        IRValue lhs = getLeft();
        IRValue rhs = getRight();


        if (lhs instanceof Int s && rhs instanceof Int l) {
            return tool.builder().integer(s.value & l.value);
        }
        if (lhs instanceof Bool s && rhs instanceof Bool l) {
            return tool.builder().bool(l.value & s.value);
        }

        if (!lhs.isConstant() && rhs.isConstant()) {
            return tool.builder().and(rhs, lhs);
        }

        if (tool.isIntegerOf(lhs, 0) && tool.isNumeric(rhs)) {
            return tool.builder().integer(0);
        }
        if (tool.isIntegerOf(rhs, 0) && tool.isNumeric(lhs)) {
            return tool.builder().integer(0);
        }

        if (tool.isBooleanOf(lhs, true) && tool.isBool(rhs)) {
            return rhs;
        }
        if (tool.isBooleanOf(rhs, true) && tool.isBool(lhs)) {
            return lhs;
        }

        if (tool.isBooleanOf(lhs, false) && tool.isBool(rhs)) {
            return lhs;
        }
        if (tool.isBooleanOf(rhs, false) && tool.isBool(lhs)) {
            return rhs;
        }

        return this;
    }


    private Stamp computeInitialStamp(IRValue l, IRValue r) {
        Stamp s1 = l.stamp;
        Stamp s2 = r.stamp;

        if (s1 instanceof IntegerStamp i1 && s2 instanceof IntegerStamp i2) {
            // For x & y, the result's max is at most the smaller of the two maxes
            // (assuming positive numbers) and min is at least 0 if both are >= 0.
            int newMax = Math.min(i1.max, i2.max);
            int newMin = (i1.min >= 0 && i2.min >= 0) ? 0 : Integer.MIN_VALUE;
            return new IntegerStamp(newMin, newMax);
        }

        if (s1 instanceof BoolStamp b1 && s2 instanceof BoolStamp b2) {
            return new BoolStamp(b1.canBeTrue & b2.canBeTrue, b1.canBeFalse | b2.canBeFalse);
        }

        return ConflictStamp.INSTANCE;
    }
}
