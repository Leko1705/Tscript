package com.tscript.runtime.jit.comp.ir.nodes;

import com.tscript.runtime.jit.comp.ir.IRValue;
import com.tscript.runtime.jit.comp.ir.IRValueVisitor;
import com.tscript.runtime.jit.comp.ir.stamps.BoolStamp;
import com.tscript.runtime.jit.comp.ir.stamps.ConflictStamp;
import com.tscript.runtime.jit.comp.ir.stamps.IntegerStamp;
import com.tscript.runtime.jit.comp.ir.stamps.Stamp;
import com.tscript.runtime.jit.comp.optim.CanonicalizerTool;

public class Or extends BinaryOp {

    public Or(IRValue left, IRValue right) {
        super(left, right);
        stamp = computeInitialStamp(left, right);
    }

    @Override
    public <P, R> R accept(IRValueVisitor<P, R> visitor, P p) {
        return visitor.visitOr(this, p);
    }

    @Override
    public String toString() {
        return "(" + getLeft().toString() + " or " + getRight().toString() + ")";
    }

    @Override
    public IRValue canonicalize(CanonicalizerTool tool) {
        IRValue lhs = getLeft();
        IRValue rhs = getRight();


        if (lhs instanceof Int s && rhs instanceof Int l) {
            return tool.builder().integer(s.value | l.value);
        }
        if (lhs instanceof Bool s && rhs instanceof Bool l) {
            return tool.builder().bool(l.value | s.value);
        }

        if (!lhs.isConstant() && rhs.isConstant()) {
            return tool.builder().or(rhs, lhs);
        }

        if (tool.isIntegerOf(lhs, 0) && tool.isNumeric(rhs)) {
            return rhs;
        }
        if (tool.isIntegerOf(rhs, 0) && tool.isNumeric(lhs)) {
            return lhs;
        }

        if (tool.isBooleanOf(lhs, true) && tool.isBool(rhs)) {
            return tool.builder().bool(true);
        }
        if (tool.isBooleanOf(rhs, true) && tool.isBool(lhs)) {
            return tool.builder().bool(true);
        }

        if (tool.isBooleanOf(lhs, false) && tool.isBool(rhs)) {
            return rhs;
        }
        if (tool.isBooleanOf(rhs, false) && tool.isBool(lhs)) {
            return lhs;
        }

        return this;
    }

    private Stamp computeInitialStamp(IRValue l, IRValue r) {
        if (l.stamp instanceof IntegerStamp i1 && r.stamp instanceof IntegerStamp i2) {
            // If both are positive, we can calculate a decent upper bound
            if (i1.min >= 0 && i2.min >= 0) {
                // Find the highest bit set in either max
                int combinedMax = i1.max | i2.max;
                // Fill all lower bits to find the "worst case" OR result
                int mask = Integer.highestOneBit(combinedMax);
                int highestPossible = (mask << 1) - 1;

                return new IntegerStamp(Math.max(i1.min, i2.min), Math.max(highestPossible, combinedMax));
            }
            // For negative numbers, bitwise logic is too chaotic for simple min/max
            return IntegerStamp.ANY;
        }

        if (l.stamp instanceof BoolStamp b1 && r.stamp instanceof BoolStamp b2) {
            return new BoolStamp(b1.canBeTrue | b2.canBeTrue, b1.canBeFalse & b2.canBeFalse);
        }

        return ConflictStamp.INSTANCE;
    }
}
