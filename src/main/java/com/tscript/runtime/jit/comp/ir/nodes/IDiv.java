package com.tscript.runtime.jit.comp.ir.nodes;

import com.tscript.runtime.jit.comp.ir.IRValue;
import com.tscript.runtime.jit.comp.ir.IRValueVisitor;
import com.tscript.runtime.jit.comp.ir.stamps.ConflictStamp;
import com.tscript.runtime.jit.comp.ir.stamps.IntegerStamp;
import com.tscript.runtime.jit.comp.ir.stamps.Stamp;
import com.tscript.runtime.jit.comp.ir.types.IntType;
import com.tscript.runtime.jit.comp.optim.CanonicalizerTool;

public class IDiv extends BinaryOp {

    public IDiv(IRValue left, IRValue right) {
        super(left, right);
        stamp = computeInitialStamp(left, right);
    }

    @Override
    public <P, R> R accept(IRValueVisitor<P, R> visitor, P p) {
        return visitor.visitIDiv(this, p);
    }

    @Override
    public String toString() {
        return "(" + getLeft().toString() + " // " + getRight().toString() + ")";
    }

    @Override
    public IRValue canonicalize(CanonicalizerTool tool) {
        IRValue lhs = getLeft();
        IRValue rhs = getRight();

        if (lhs instanceof Int s && rhs instanceof Int l) {
            return tool.builder().integer(s.value / l.value);
        }

        if (!lhs.isConstant() && rhs.isConstant()) {
            return tool.builder().idiv(rhs, lhs);
        }

        if (tool.isIntegerOf(lhs, 0) && tool.type(rhs) == IntType.INSTANCE) {
            return lhs;
        }
        if (tool.isIntegerOf(rhs, 1) && tool.type(lhs) == IntType.INSTANCE) {
            return lhs;
        }

        return this;
    }

    private Stamp computeInitialStamp(IRValue l, IRValue r) {
        if (l.stamp instanceof IntegerStamp i1 && r.stamp instanceof IntegerStamp i2) {
            // Safety: If the divisor could be zero, the result is unpredictable (or an error)
            if (i2.min <= 0 && i2.max >= 0) {
                return IntegerStamp.ANY;
            }

            // Four corners check (similar to Mul)
            long v1 = (long) i1.min / i2.min;
            long v2 = (long) i1.min / i2.max;
            long v3 = (long) i1.max / i2.min;
            long v4 = (long) i1.max / i2.max;

            int min = (int) Math.min(Math.min(v1, v2), Math.min(v3, v4));
            int max = (int) Math.max(Math.max(v1, v2), Math.max(v3, v4));

            return new IntegerStamp(min, max);
        }
        return ConflictStamp.INSTANCE;
    }
}
