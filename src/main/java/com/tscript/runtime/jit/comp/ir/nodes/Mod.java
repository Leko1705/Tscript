package com.tscript.runtime.jit.comp.ir.nodes;

import com.tscript.runtime.jit.comp.ir.IRValue;
import com.tscript.runtime.jit.comp.ir.IRValueVisitor;
import com.tscript.runtime.jit.comp.ir.types.IntType;
import com.tscript.runtime.jit.comp.optim.CanonicalizerTool;

public class Mod extends BinaryOp {

    public Mod(IRValue left, IRValue right) {
        super(left, right);
    }

    @Override
    public <P, R> R accept(IRValueVisitor<P, R> visitor, P p) {
        return visitor.visitMod(this, p);
    }

    @Override
    public String toString() {
        return "(" + getLeft().toString() + " % " + getRight().toString() + ")";
    }

    @Override
    public IRValue canonicalize(CanonicalizerTool tool) {
        IRValue lhs = getLeft();
        IRValue rhs = getRight();

        if (lhs instanceof Int s && rhs instanceof Int l) {
            return tool.builder().integer(s.value % l.value);
        }

        if (!lhs.isConstant() && rhs.isConstant()) {
            return tool.builder().mod(rhs, lhs);
        }

        if (tool.isIntegerOf(lhs, 0) && tool.type(rhs) == IntType.INSTANCE) {
            return lhs;
        }
        if ((tool.isIntegerOf(rhs, 0) || tool.isIntegerOf(rhs, 1))
                && tool.type(lhs) == IntType.INSTANCE) {
            return tool.builder().integer(0);
        }

        return this;
    }
}
